package com.study.productservice.service.dto;

import com.study.productservice.entity.Product;

import java.time.Instant;

public record ProductResult(
		String productId,
		String productName,
		Long unitPrice,
		Integer stock,
		Instant createdAt) {

	public static ProductResult from(Product product) {
		return new ProductResult(
				product.getProductId(),
				product.getProductName(),
				product.getUnitPrice(),
				product.getStock(),
				product.getCreatedAt());
	}
}
