package com.study.productservice.controller.dto;

import com.study.productservice.service.dto.ProductResult;

import java.time.Instant;

public record ProductResponse(
        String productId,
        String productName,
        Long unitPrice,
        Integer stock,
        Instant createdAt) {

    public static ProductResponse from(ProductResult product) {
        return new ProductResponse(
                product.productId(),
                product.productName(),
                product.unitPrice(),
                product.stock(),
                product.createdAt());
    }
}
