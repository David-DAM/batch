package com.david.batch.listener;

import com.david.batch.domain.Product;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReaderListener implements ItemReadListener<Product> {

    private final Stats stats;
    @Override
    public void beforeRead() {
        //ItemReadListener.super.beforeRead();
    }

    @Override
    public void afterRead(Product item) {
        //ItemReadListener.super.afterRead(item);
    }

    @Override
    public void onReadError(Exception ex) {
        stats.addError();
    }
}
