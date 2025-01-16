package com.maven.pos.entities.dto.helper;

import com.maven.pos.entities.Item;
import com.maven.pos.entities.Item2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemItem2DtoHelper {

    private Item item;
    private Item2 item2;
}
