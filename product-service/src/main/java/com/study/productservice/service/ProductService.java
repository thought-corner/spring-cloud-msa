package com.study.productservice.service;

import com.study.productservice.repository.ProductRepository;
import com.study.productservice.service.dto.ProductResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductResult> getProducts() {
		return productRepository.findAll().stream()
				.map(ProductResult::from)
				.toList();
	}

	public Optional<ProductResult> getProduct(String productId) {
		return productRepository.findByProductId(productId)
				.map(ProductResult::from);
	}
}
