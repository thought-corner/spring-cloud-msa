package com.study.productservice.service;

import com.study.productservice.service.dto.ProductResult;
import com.study.productservice.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class ProductServiceTest {

	@Autowired
	private ProductService productService;

	@Test
	void 초기_데이터의_상품_식별자는_모두_PRODUCT_접두어를_가진다() {
		List<ProductResult> products = productService.getProducts();

		assertThat(products).isNotEmpty();
		assertThat(products).allSatisfy(product ->
				assertThat(product.productId()).startsWith("PRODUCT-"));
	}

	@Test
	void 존재하지_않는_상품_식별자로_조회하면_빈_결과를_반환한다() {
		Optional<ProductResult> found = productService.getProduct("NO-SUCH-PRODUCT");

		assertThat(found).isEmpty();
	}
}
