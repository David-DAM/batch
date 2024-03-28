package com.david.batch.listener;

import com.david.batch.domain.Category;
import com.david.batch.domain.Product;
import com.david.batch.domain.ProductDTO;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessorListener implements ItemProcessListener<Product, ProductDTO> {

    private final Stats stats;
    private int computers = 0;
    private int phones = 0;


    @Override
    public void beforeProcess(Product item) {
        //ItemProcessListener.super.beforeProcess(item);
    }

    @Override
    public void afterProcess(Product item, ProductDTO result) {
        switch (Objects.requireNonNull(result).getCategory()){
            case COMPUTER -> stats.getCategoryQuantities().put(Category.COMPUTER, ++computers);
            case PHONE -> stats.getCategoryQuantities().put(Category.PHONE, ++phones);
            default -> log.info("Product {} without category",result.getId());
        }
    }

    @Override
    public void onProcessError(Product item, Exception e) {
        stats.addError();
    }
}
