package com.study.orderservice.domain;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

	@Test
	void 주문을_생성하면_총액은_단가와_수량의_곱으로_파생된다() {
		Order order = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(3), Money.of(1500), NOW);

		assertThat(order.totalPrice()).isEqualTo(Money.of(4500));
	}

	@Test
	void 주문에는_전달받은_생성시각이_기록된다() {
		Order order = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(1), Money.of(1000), NOW);

		assertThat(order.createdAt()).isEqualTo(NOW);
	}

	@Test
	void 주문을_생성하면_주문번호가_자동으로_부여된다() {
		Order order = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(1), Money.of(1000), NOW);

		assertThat(order.orderId()).isNotNull();
		assertThat(order.orderId().value()).isNotBlank();
	}

	@Test
	void 서로_다른_주문은_서로_다른_주문번호를_갖는다() {
		Order first = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(1), Money.of(1000), NOW);
		Order second = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(1), Money.of(1000), NOW);

		assertThat(first.orderId()).isNotEqualTo(second.orderId());
	}

	@Test
	void 수량이_0이하이면_주문을_만들_수_없다() {
		assertThatThrownBy(() -> Quantity.of(0))
				.isInstanceOf(OrderException.class)
				.hasMessageContaining("quantity must be greater than zero")
				.extracting(errorCodeOf())
				.isEqualTo(OrderErrorCode.INVALID_QUANTITY);
	}

	@Test
	void 단가가_0이면_주문을_만들_수_없다() {
		assertThatThrownBy(() -> Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(1), Money.of(0), NOW))
				.isInstanceOf(OrderException.class)
				.extracting(errorCodeOf())
				.isEqualTo(OrderErrorCode.NON_POSITIVE_UNIT_PRICE);
	}

	@Test
	void 금액이_음수이면_주문을_만들_수_없다() {
		assertThatThrownBy(() -> Money.of(-1))
				.isInstanceOf(OrderException.class)
				.extracting(errorCodeOf())
				.isEqualTo(OrderErrorCode.NEGATIVE_AMOUNT);
	}

	@Test
	void 상품_식별자가_비어_있으면_주문을_만들_수_없다() {
		assertThatThrownBy(() -> ProductId.of("  "))
				.isInstanceOf(OrderException.class)
				.hasMessageContaining("identifier must not be blank")
				.extracting(errorCodeOf())
				.isEqualTo(OrderErrorCode.BLANK_IDENTIFIER);
	}

	@Test
	void int_범위를_넘는_금액도_다룰_수_있다() {
		Order order = Order.place(UserId.of("user-1"), ProductId.of("PRODUCT-001"),
				Quantity.of(2), Money.of(3_000_000_000L), NOW);

		assertThat(order.totalPrice()).isEqualTo(Money.of(6_000_000_000L));
	}

	@Test
	void 총액_곱셈이_long_범위를_넘으면_조용히_넘치지_않고_잘못된_입력으로_거부된다() {
		assertThatThrownBy(() -> Money.of(Long.MAX_VALUE).multiply(Quantity.of(2)))
				.isInstanceOf(OrderException.class)
				.hasMessageContaining("amount is too large")
				.extracting(errorCodeOf())
				.isEqualTo(OrderErrorCode.AMOUNT_OVERFLOW);
	}

	@Test
	void 모든_오류코드는_400_응답과_ORDER_접두어와_메시지를_가진다() {
		for (OrderErrorCode errorCode : OrderErrorCode.values()) {
			assertThat(errorCode.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(errorCode.code()).startsWith("ORDER-");
			assertThat(errorCode.message()).isNotBlank();
		}
	}

	@Test
	void 오류코드는_서로_중복되지_않는다() {
		assertThat(OrderErrorCode.values())
				.extracting(OrderErrorCode::code)
				.doesNotHaveDuplicates();
	}

	private static java.util.function.Function<Throwable, ErrorCode> errorCodeOf() {
		return throwable -> ((OrderException) throwable).errorCode();
	}
}
