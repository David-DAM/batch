package com.david.batch.domain;

import org.springframework.stereotype.Component;

import java.util.function.Function;
@Component
public class ProductDTOMapper implements Function<Product, ProductDTO> {
    @Override
    public ProductDTO apply(Product product) {
        return new ProductDTO.ProductDTOBuilder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .build();
    }
}
