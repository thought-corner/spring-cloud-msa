package com.study.productservice.controller;

import com.study.productservice.controller.dto.ProductResponse;
import com.study.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getProducts() {
		List<ProductResponse> result = productService.getProducts().stream()
				.map(ProductResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponse> getProduct(@PathVariable("productId") String productId) {
		return productService.getProduct(productId)
				.map(product -> ResponseEntity.ok(ProductResponse.from(product)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
