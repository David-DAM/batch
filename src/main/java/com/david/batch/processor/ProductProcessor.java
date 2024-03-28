package com.david.batch.processor;

import com.david.batch.domain.Category;
import com.david.batch.domain.Product;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductProcessor implements ItemProcessor<Product,Product> {

    @Override
    public Product process(Product item) throws Exception {
        return item;
    }
}
