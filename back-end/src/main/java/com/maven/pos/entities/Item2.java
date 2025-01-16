package com.maven.pos.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "item_table2")
//@ToString
public class Item2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(name = "item_name", unique = true)
    private String itemName;

    @Column(name = "item_price")
    private Double itemPrice;

    private LocalDate date;

    private LocalTime time;

//    @Column(name = "image_name")
//    private String imageName;

    @Column(name = "image_url")
    private String imageUrl;

//    @ManyToOne
//    @JoinColumn(name = "item2_id")
//    private Item item;

    @Transient
    private String imageBase64;

    @OneToMany(mappedBy = "item2", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SaleItem> saleItems;

    @OneToMany(mappedBy = "item2", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SaleTopping> saleToppings;

    private Boolean isToppingSelected;
}
