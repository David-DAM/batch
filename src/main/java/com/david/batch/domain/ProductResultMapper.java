package com.david.batch.domain;

import com.david.batch.domain.Product;
import com.david.batch.domain.ProductResult;
import org.springframework.stereotype.Component;

import java.util.function.Function;
@Component
public class ProductResultMapper implements Function<Product, ProductResult> {
    @Override
    public ProductResult apply(Product product) {
        return new ProductResult.ProductResultBuilder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .build();
    }
}
