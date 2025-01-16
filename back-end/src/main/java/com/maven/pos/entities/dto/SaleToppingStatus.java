package com.maven.pos.entities.dto;

import com.maven.pos.entities.Item;
import com.maven.pos.entities.Item2;
import com.maven.pos.entities.Sale;
import com.maven.pos.entities.Topping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleToppingStatus {

    private Sale sale;

    private Item item;
    private Item2 item2;

    private Topping topping;
}
