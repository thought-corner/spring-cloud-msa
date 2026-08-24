package com.study.productservice.repository;

import com.study.productservice.entity.Product;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ProductRepository extends ListCrudRepository<Product, Long> {

	Optional<Product> findByProductId(String productId);
}
