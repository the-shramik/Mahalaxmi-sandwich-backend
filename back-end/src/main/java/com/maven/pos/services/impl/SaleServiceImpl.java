package com.maven.pos.services.impl;

import com.maven.pos.entities.*;
import com.maven.pos.entities.dto.SaleItemReport;
import com.maven.pos.entities.dto.SaleItemStatusOrders;
import com.maven.pos.entities.dto.SaleItems;
import com.maven.pos.entities.dto.helper.SaleItemsHelper;
import com.maven.pos.repositories.*;
import com.maven.pos.services.IItemOrderService;
import com.maven.pos.services.ISaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class SaleServiceImpl implements ISaleService {
    @Autowired
    private ISaleRepository saleRepository;

    @Autowired
    private IItemOrderService itemOrderService;

    @Autowired
    private IItemOrderRepository itemOrderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRepository2 itemRepository2;

    @Autowired
    private IToppingRepository toppingRepository;


    @Override
    public SaleItems addSale(SaleItems saleItemsDto) {
        // Create and set up Sale entity
        Sale sale = new Sale();
        sale.setSubTotal(saleItemsDto.getSubTotal());
        sale.setExtraCharges(saleItemsDto.getExtraCharges());
        sale.setParcelCharges(saleItemsDto.getParcelCharges());
        sale.setFinalTotal(saleItemsDto.getFinalTotal());
        sale.setPaymentMode(saleItemsDto.getPaymentMode().toUpperCase());
        sale.setItemSaleDate(LocalDate.now());
        sale.setItemSaleTime(LocalTime.now());

        // Create lists to hold sale items and toppings
        List<SaleItem> saleItems = new ArrayList<>();
        List<SaleTopping> saleToppings = new ArrayList<>();

        // Save Sale first to generate saleId
        Sale savedSale = saleRepository.save(sale);

        // Iterate through sale item helpers to populate sale items and toppings
        for (SaleItemsHelper helper : saleItemsDto.getSaleItemsHelpers()) {
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(savedSale); // Associate with the saved Sale
            saleItem.setItem(helper.getItem());
            saleItem.setItem2(helper.getItem2());
            saleItem.setQuantity(helper.getSaleQty());
            saleItems.add(saleItem);

            // Check if toppings are present
            if (helper.getItem2()!=null) {
                System.out.println("Inside if");
                for (Topping topping : toppingRepository.findAll()) {
                    System.out.println("foreach");
                    SaleTopping saleTopping = new SaleTopping();
                    saleTopping.setSale(savedSale); // Associate with the saved Sale
                    saleTopping.setTopping(topping);

                    saleTopping.setItem2(helper.getItem2());
                    saleToppings.add(saleTopping);

                    // Pass saleId, itemId, and toppingId to addItemOrders
                    itemOrderService.addItemOrders(savedSale.getSaleId(),null, helper.getItem2().getItemId(), topping.getToppingId(), saleItem.getQuantity());
                }
            } else {
                    itemOrderService.addItemOrders(savedSale.getSaleId(), helper.getItem().getItemId(), null,null, saleItem.getQuantity());
            }
        }

        sale.setSaleItems(saleItems);
        sale.setSaleToppings(saleToppings);

        // Save the updated Sale entity with its associated items and toppings
        saleRepository.save(savedSale);

        saleItemsDto.setBillNumber(savedSale.getSaleId());
        // Return SaleItems DTO
        return saleItemsDto;

    }

    @Override
    public Integer totalCashCounterOrders() {
        return saleRepository.countSalesByDateAndPaymentMode(LocalDate.now(), "CASH");
    }

    @Override
    public Integer totalUpiCounterOrders() {
        return saleRepository.countSalesByDateAndPaymentMode(LocalDate.now(), "UPI");
    }

    @Override
    public Double grandTotalUpiCounterOrders() {
        return saleRepository.getGrandTotalOfSalesByDateAndPaymentMode(LocalDate.now(), "UPI");
    }

    @Override
    public Double grandTotalCashCounterOrders() {
        return saleRepository.getGrandTotalOfSalesByDateAndPaymentMode(LocalDate.now(), "CASH");
    }

    @Override
    public List<SaleItemStatusOrders> getPendingOrdersKitchen() {

        List<SaleItemStatusOrders> saleItemStatusOrders = new ArrayList<>();

        itemOrderRepository.findAllByStatusFalseAndCreatedDate(LocalDate.now()).forEach(itemOrder -> {
            SaleItemStatusOrders saleItemStatusOrder = new SaleItemStatusOrders();
//            Sale sale=saleRepository.findSaleBySaleIdAndItemIdAndToppingId(itemOrder.getSaleId(),itemOrder.getItemId(),itemOrder.getToppingId());

            saleItemStatusOrder.setOrderStatus(itemOrder.isStatus());
            saleItemStatusOrder.setBillNumber(itemOrder.getSaleId());

            if(itemOrder.getItemId()!=null) {
                Optional<Item> item = itemRepository.findById(itemOrder.getItemId());
                if (item.isPresent()) {
                    saleItemStatusOrder.setItemId(item.get().getItemId());
                    saleItemStatusOrder.setItemId2(null);
                    saleItemStatusOrder.setItemName(item.get().getItemName());
                    saleItemStatusOrder.setImageUrl(item.get().getImageUrl());
                    saleItemStatusOrder.setQuantity(itemOrder.getQuantity());

                } else {
                    System.out.println("*");
                }
            }else {
                Optional<Item2> item2 = itemRepository2.findById(itemOrder.getItemId2());
                if (item2.isPresent()) {
                    saleItemStatusOrder.setItemName(item2.get().getItemName());
                    saleItemStatusOrder.setItemId2(item2.get().getItemId());
                    saleItemStatusOrder.setItemId(null);
                    saleItemStatusOrder.setImageUrl(item2.get().getImageUrl());
                    saleItemStatusOrder.setQuantity(itemOrder.getQuantity());
                    saleItemStatusOrder.setToppingName("Cheese");
                    saleItemStatusOrder.setToppingId(1L);
                } else {
                    System.out.println("*");
                }
            }


            saleItemStatusOrders.add(saleItemStatusOrder);

        });
        return saleItemStatusOrders;
    }

    @Override
    public List<SaleItemReport> getPendingOrdersCounter() {
        List<SaleItemReport> saleItemReports = new ArrayList<>();

        itemOrderRepository.findByStatusFalseAndItemOrderDate(LocalDate.now()).forEach(itemOrder -> {
            Sale sale = null;

            if (itemOrder.getToppingId() != null) {
                sale = saleRepository.findSaleBySaleIdAndItemIdAndToppingId(itemOrder.getSaleId(), itemOrder.getItemId(), itemOrder.getToppingId());
            } else {
                sale = saleRepository.findSaleBySaleIdAndItemId(itemOrder.getSaleId(), itemOrder.getItemId());
            }

            // Logging for debugging
            System.out.println("ItemOrder: " + itemOrder);
            if (sale != null) {
                System.out.println("Sale found: " + sale);
                Sale finalSale = sale;
                sale.getSaleItems().forEach(saleItem -> {
                    SaleItemReport saleItemReport = new SaleItemReport();
                    saleItemReport.setItemName(saleItem.getItem().getItemName());
                    saleItemReport.setQuantity(saleItem.getQuantity());
                    saleItemReport.setBillNumber(finalSale.getSaleId());
                    saleItemReport.setParcelCharges(finalSale.getParcelCharges());
                    saleItemReport.setFinalTotal(finalSale.getFinalTotal());
                    saleItemReport.setPaymentMode(finalSale.getPaymentMode());
                    saleItemReport.setSubTotal(finalSale.getSubTotal());
                    saleItemReport.setExtraCharges(finalSale.getExtraCharges());
                    saleItemReports.add(saleItemReport);
                });
            } else {
                System.out.println("Sale not found for SaleId: " + itemOrder.getSaleId() +
                        ", ItemId: " + itemOrder.getItemId() +
                        ", ToppingId: " + itemOrder.getToppingId());
            }
        });

        System.out.println(saleItemReports);
        return null;
    }


    @Override
    public List<SaleItemReport> getCompletedOrdersCounter() {
        List<SaleItemReport> saleItemReports = new ArrayList<>();

        itemOrderRepository.findByStatusTrueAndItemOrderDate(LocalDate.now()).forEach(itemOrder -> {
            Sale sale = null;

            if (itemOrder.getToppingId() != null) {
                sale = saleRepository.findSaleBySaleIdAndItemIdAndToppingId(itemOrder.getSaleId(), itemOrder.getItemId(), itemOrder.getToppingId());
            } else {
                sale = saleRepository.findSaleBySaleIdAndItemId(itemOrder.getSaleId(), itemOrder.getItemId());
            }

            // Logging for debugging
            System.out.println("ItemOrder: " + itemOrder);
            if (sale != null) {
                System.out.println("Sale found: " + sale);
                Sale finalSale = sale;
                sale.getSaleItems().forEach(saleItem -> {
                    SaleItemReport saleItemReport = new SaleItemReport();
                    saleItemReport.setItemName(saleItem.getItem().getItemName());
                    saleItemReport.setQuantity(saleItem.getQuantity());
                    saleItemReport.setBillNumber(finalSale.getSaleId());
                    saleItemReport.setParcelCharges(finalSale.getParcelCharges());
                    saleItemReport.setFinalTotal(finalSale.getFinalTotal());
                    saleItemReport.setPaymentMode(finalSale.getPaymentMode());
                    saleItemReport.setSubTotal(finalSale.getSubTotal());
                    saleItemReport.setExtraCharges(finalSale.getExtraCharges());
                    saleItemReports.add(saleItemReport);
                });
            } else {
                System.out.println("Sale not found for SaleId: " + itemOrder.getSaleId() +
                        ", ItemId: " + itemOrder.getItemId() +
                        ", ToppingId: " + itemOrder.getToppingId());
            }
        });

        return saleItemReports;
    }

}
