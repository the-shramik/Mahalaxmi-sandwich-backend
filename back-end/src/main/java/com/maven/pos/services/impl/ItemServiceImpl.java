package com.maven.pos.services.impl;

import com.maven.pos.entities.Item;
import com.maven.pos.entities.Item2;
import com.maven.pos.entities.Topping;
import com.maven.pos.entities.dto.helper.ItemItem2DtoHelper;
import com.maven.pos.repositories.IToppingRepository;
import com.maven.pos.repositories.ItemRepository;
import com.maven.pos.repositories.ItemRepository2;
import com.maven.pos.services.IItemService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class ItemServiceImpl implements IItemService {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRepository2 itemRepository2;

    @Autowired
    private IToppingRepository toppingRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @Override
    public Item addItem(Item item, MultipartFile imageFile) throws IOException {

        String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        File file = convertMultipartFileToFile(imageFile);
        String bucketName = "springboot-test-0076";

        // Upload file to S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(imageName)
                .build();

        PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

        file.delete(); // Remove temporary file after upload

        // Get S3 URL
        String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName);

        item.setImageUrl(imageUrl);
        item.setDate(LocalDate.now());
        item.setTime(LocalTime.now());

        if(item.isToppingPresent()){
            Item2 toppingItem=new Item2();
            toppingItem.setItemName(item.getItemName()+"+"+"Cheese");

            Double toppingPrice=0.0;
            for(Topping topping:toppingRepository.findAll()){
                toppingPrice+=topping.getToppingPrice();
            }
            toppingItem.setItemPrice(item.getItemPrice()+toppingPrice);
            toppingItem.setImageUrl(imageUrl);
            toppingItem.setDate(LocalDate.now());
            toppingItem.setTime(LocalTime.now());
            Item2 savedItem2=itemRepository2.save(toppingItem);
            item.setItem2(savedItem2);
        }

        return itemRepository.save(item);
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    @Override
    public List<ItemItem2DtoHelper> getAllItems() {

        List<ItemItem2DtoHelper> itemItem2DtoHelpers=new ArrayList<>();
        itemRepository.findAll().forEach(item -> {
            ItemItem2DtoHelper itemItem2DtoHelper=new ItemItem2DtoHelper();
            itemItem2DtoHelper.setItem(item);

            if(item.isToppingPresent()){
                itemItem2DtoHelper.setItem2(itemRepository2.findById(item.getItem2().getItemId()).get());
            }

            itemItem2DtoHelpers.add(itemItem2DtoHelper);
        });

        return itemItem2DtoHelpers;
    }

    @Override
    public Item getItemById(Item item) {
        return itemRepository.findById(item.getItemId()).get();
    }


    @Override
    public Item deleteItem(Long itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (itemOptional.isEmpty()) {
            throw new RuntimeException("Item not found with id: " + itemId);
        }

        Item item = itemOptional.get();

        String imageName = item.getImageUrl().substring(item.getImageUrl().lastIndexOf('/') + 1);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket("springboot-test-0076")
                .key(imageName)
                .build());

        if (item.getItem2() != null) {
            List<Item> dependentItems = itemRepository.findByItem2_ItemId(item.getItem2().getItemId());
            if (!dependentItems.isEmpty()) {
                itemRepository.deleteAll(dependentItems);
            }

            itemRepository2.deleteById(item.getItem2().getItemId());
        }

        itemRepository.deleteById(itemId);

        return item;
    }


    @Override
    public Item updateItem(Item item,MultipartFile imageFile) throws IOException {
        Item it=itemRepository.findById(item.getItemId()).get();

        if (!imageFile.isEmpty()){
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            File file = convertMultipartFileToFile(imageFile);
            String bucketName = "springboot-test-0076";

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageName)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            file.delete(); // Remove temporary file after upload

            // Get S3 URL
            String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName);
            it.setImageUrl(imageUrl);

            if(it.getItem2()!=null){
                Item2 it2 = itemRepository2.findById(it.getItem2().getItemId()).get();
                it2.setImageUrl(imageUrl);
                it2.setItemName(item.getItemName()+"+"+"Cheese");
                Double toppingPrice=0.0;
                for(Topping topping:toppingRepository.findAll()){
                    toppingPrice+=topping.getToppingPrice();
                }
                it2.setItemPrice(item.getItemPrice()+toppingPrice);
            }
        }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        String filePath = Paths.get(imageUploadDir, uniqueFileName).toString();

        // Save the file to the specified directory
        Files.createDirectories(Paths.get(imageUploadDir)); // Ensure the directory exists
        imageFile.transferTo(new File(filePath));

        it.setItemName(item.getItemName());
        it.setItemPrice(item.getItemPrice());
        it.setToppingPresent(it.isToppingPresent());

        return itemRepository.save(it);
    }

}
