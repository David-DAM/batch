package com.david.batch.config;

import com.david.batch.domain.Product;
import org.springframework.batch.core.ItemProcessListener;

public class Listener implements ItemProcessListener<Product,Product> {

    private int errors = 0;
    private int processes = 0;

    @Override
    public void beforeProcess(Product item) {
        ItemProcessListener.super.beforeProcess(item);
    }

    @Override
    public void afterProcess(Product item, Product result) {
        processes++;
    }

    @Override
    public void onProcessError(Product item, Exception e) {
        errors++;
    }
}
