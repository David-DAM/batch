package com.david.batch.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String category;

}
