package com.study.userservice.service.dto;

public record UserCreateCommand(String email, String name, String password) {
}
