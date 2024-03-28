package com.david.batch.processor;

import com.david.batch.domain.Product;
import com.david.batch.domain.ProductDTO;
import com.david.batch.domain.ProductDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductDTOProcessor implements ItemProcessor<Product, ProductDTO> {

    private final ProductDTOMapper mapper;

    @Override
    public ProductDTO process(Product item) throws Exception {

        return mapper.apply(item);
    }
}
