package com.david.batch.listener;

import com.david.batch.domain.Product;
import com.david.batch.domain.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessorListener implements ItemProcessListener<Product, ProductDTO> {

    @Override
    public void beforeProcess(Product item) {
        //ItemProcessListener.super.beforeProcess(item);
    }

    @Override
    public void afterProcess(Product item, ProductDTO result) {

    }

    @Override
    public void onProcessError(Product item, Exception e) {

    }
}
