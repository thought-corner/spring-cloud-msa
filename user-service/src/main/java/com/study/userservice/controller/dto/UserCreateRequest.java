package com.study.userservice.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
		@NotNull(message = "Email cannot be null")
		@Size(min = 2, message = "Email must not be less than two characters")
		@Email
		String email,

		@NotNull(message = "Name cannot be null")
		@Size(min = 2, message = "Name must not be less than two characters")
		String name,

		@NotNull(message = "Password cannot be null")
		@Size(min = 8, message = "Password must be equal to or greater than 8 characters")
		String pwd) {
}
