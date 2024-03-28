package com.david.batch.listener;

import com.david.batch.domain.Product;
import com.david.batch.domain.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WriterListener implements ItemWriteListener<Product> {

    private final Stats stats;

    @Override
    public void beforeWrite(Chunk items) {
        //ItemWriteListener.super.beforeWrite(items);
    }

    @Override
    public void afterWrite(Chunk items) {
        stats.addProcessed(items.size());
    }

    @Override
    public void onWriteError(Exception exception, Chunk items) {
        stats.addError();
    }
}
