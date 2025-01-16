package com.maven.pos.services.impl;

import com.maven.pos.entities.ItemOrders;
import com.maven.pos.entities.TempSale;
import com.maven.pos.entities.dto.SaleToppingStatus;
import com.maven.pos.repositories.IItemOrderRepository;
import com.maven.pos.repositories.ITempSaleRepository;
import com.maven.pos.services.IItemOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ItemOrderImpl implements IItemOrderService {

    @Autowired
    private IItemOrderRepository itemOrderRepository;

    @Autowired
    private ITempSaleRepository tempSaleRepository;

    @Override
    public ItemOrders addItemOrders(Long saleId, Long itemId, Long itemId2, Long toppingId, Integer quantity) {

        ItemOrders itemOrders = new ItemOrders();
        itemOrders.setSaleId(saleId);
        itemOrders.setItemId(itemId);
        itemOrders.setItemId2(itemId2);
        itemOrders.setToppingId(toppingId);
        itemOrders.setStatus(false);
        itemOrders.setQuantity(quantity);
        itemOrders.setItemOrderDate(LocalDate.now());
        return itemOrderRepository.save(itemOrders);
    }

    @Override
    public int updateOrderStatus(SaleToppingStatus saleToppingStatus) {

        if (saleToppingStatus.getTopping() != null) {
            TempSale tempSale = new TempSale();
            tempSale.setStatus(false);
            tempSale.setBillNumber(saleToppingStatus.getSale().getSaleId());
            tempSale.setToppingId(saleToppingStatus.getTopping().getToppingId());

            if (saleToppingStatus.getItem() != null) {
                tempSale.setItemId(saleToppingStatus.getItem().getItemId());
            }

            if (saleToppingStatus.getItem2() != null) {
                tempSale.setItemId2(saleToppingStatus.getItem2().getItemId());
            }

            tempSaleRepository.save(tempSale);

            System.out.println("************"+saleToppingStatus.getItem().getItemId()+"**"+saleToppingStatus.getItem2().getItemId());
            if (saleToppingStatus.getItem().getItemId() != 0) {

                return itemOrderRepository.updateStatusByItemIdAndToppingIdAndSaleId(true, saleToppingStatus.getItem().getItemId(),
                        saleToppingStatus.getTopping().getToppingId(), saleToppingStatus.getSale().getSaleId());
            } else if (saleToppingStatus.getItem2().getItemId() != 0) {
                return itemOrderRepository.updateStatusByItemId2AndToppingIdAndSaleId(true, saleToppingStatus.getItem2().getItemId(),
                        1L, saleToppingStatus.getSale().getSaleId());
            }else {
                return 0;
            }

        } else {

            TempSale tempSale = new TempSale();
            tempSale.setStatus(false);
            tempSale.setBillNumber(saleToppingStatus.getSale().getSaleId());
            tempSale.setToppingId(null);
            if (saleToppingStatus.getItem() != null) {
                tempSale.setItemId(saleToppingStatus.getItem().getItemId());
            }

            if (saleToppingStatus.getItem2() != null) {
                tempSale.setItemId2(saleToppingStatus.getItem2().getItemId());
            }
            tempSaleRepository.save(tempSale);

            if (saleToppingStatus.getItem() != null) {
                return itemOrderRepository.updateStatusByItemIdAndToppingIdAndSaleId(true, saleToppingStatus.getItem().getItemId(),
                        null, saleToppingStatus.getSale().getSaleId());
            } else if (saleToppingStatus.getItem2() != null) {
                return itemOrderRepository.updateStatusByItemId2AndToppingIdAndSaleId(true, saleToppingStatus.getItem2().getItemId(),
                        null, saleToppingStatus.getSale().getSaleId());
            }else {
                return 0;
            }
        }
    }


    @Override
    public void removeOrder(SaleToppingStatus saleToppingStatus) {

        if(saleToppingStatus.getItem()!=null) {
            System.out.println("IF1");
            itemOrderRepository.deleteItemOrder(saleToppingStatus.getSale().getSaleId(), saleToppingStatus.getItem().getItemId(), saleToppingStatus.getTopping().getToppingId());
        }

        if(saleToppingStatus.getItem2()!=null){
            System.out.println("IF2");
            System.out.println(saleToppingStatus.getSale().getSaleId()+"===="+saleToppingStatus.getItem2().getItemId());
            itemOrderRepository.deleteItemOrder2(saleToppingStatus.getSale().getSaleId(), saleToppingStatus.getItem2().getItemId(), 1L);
        }
    }
}
