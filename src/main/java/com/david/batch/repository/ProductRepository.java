package com.david.batch.repository;

import com.david.batch.domain.Category;
import com.david.batch.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Long countAllByCategory(Category category);
}
