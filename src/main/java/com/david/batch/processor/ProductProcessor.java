package com.david.batch.processor;

import com.david.batch.domain.Product;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class ProductProcessor implements ItemProcessor<Product,Product> {

    private final Stats stats;
    @Override
    public Product process(Product item) throws Exception {
        stats.addProcessed();
        return item;
    }
}
