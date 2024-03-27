package com.david.batch.processor;

import com.david.batch.domain.Product;
import com.david.batch.domain.ProductResult;
import com.david.batch.domain.ProductResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ResultProcessor implements ItemProcessor<Product, ProductResult> {

    private final ProductResultMapper mapper;

    @Override
    public ProductResult process(Product item) throws Exception {

        return mapper.apply(item);
    }
}
