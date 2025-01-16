package com.maven.pos.controllers;

import com.maven.pos.entities.Item;
import com.maven.pos.entities.Topping;
import com.maven.pos.entities.User;
import com.maven.pos.enums.UserRole;
import com.maven.pos.services.IItemService;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/item")
@CrossOrigin("*")
public class ItemController {

    @Autowired
    private IItemService itemService;

    @PostMapping("/add-item")
    public ResponseEntity<Item> generateItem(@ModelAttribute("itemName") String itemName,
                                             @ModelAttribute("itemPrice") Double itemPrice,
                                             @ModelAttribute("isToppingPresent") Boolean isToppingPresent,
                                             @RequestParam("image")MultipartFile file) throws IOException {


        Item item=new Item();
        item.setItemName(itemName);
        item.setItemPrice(itemPrice);
        item.setToppingPresent(isToppingPresent);

        return ResponseEntity.ok(itemService.addItem(item,file));
    }


    @GetMapping("/get-items")
    private ResponseEntity<?> getAllItems(){
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @PostMapping("/get-item")
    private ResponseEntity<?> getItemById(@RequestBody Item item){
        return ResponseEntity.ok(itemService.getItemById(item));
    }

    @DeleteMapping("/delete-item/{itemId}")
    private ResponseEntity<Item> deleteItemById(@PathVariable("itemId") Long itemId){
      return ResponseEntity.ok(itemService.deleteItem(itemId));
    }

    @PatchMapping("/update-item")
    private ResponseEntity<?> updateItem(@ModelAttribute("itemId") Long itemId,
                                         @ModelAttribute("itemName") String itemName,
                                         @ModelAttribute("itemPrice") Double itemPrice,
                                         @RequestParam(value="isToppingPresent",required = false) @Nullable Boolean isToppingPresent,
                                         @RequestParam(value = "image",required = false) @Nullable MultipartFile file) throws IOException {

        Item item=new Item();
        item.setItemName(itemName);
        item.setItemPrice(itemPrice);
        item.setItemId(itemId);
        return ResponseEntity.ok(itemService.updateItem(item,file));
    }

}
