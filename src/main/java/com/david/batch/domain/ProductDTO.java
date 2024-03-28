package com.david.batch.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Category category;

}
