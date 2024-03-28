package com.david.batch.writer;

import com.david.batch.domain.Product;
import com.david.batch.domain.Stats;
import com.david.batch.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DatabaseWriter implements ItemWriter<Product> {

    private final ProductRepository productRepository;
    private final Stats stats;

    @Override
    public void write(Chunk<? extends Product> chunk) throws Exception {
        List<? extends Product> items = chunk.getItems();

        List<? extends Product> saved = productRepository.saveAll(items);

        stats.getWrittenItems().addAll(saved);
    }
}
