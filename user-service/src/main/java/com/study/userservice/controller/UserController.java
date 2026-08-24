package com.study.userservice.controller;

import com.study.userservice.controller.dto.UserDetailResponse;
import com.study.userservice.controller.dto.UserResponse;
import com.study.userservice.service.UserService;
import com.study.userservice.service.dto.UserCreateCommand;
import com.study.userservice.controller.dto.UserCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest user) {
        UserCreateCommand command = new UserCreateCommand(user.email(), user.name(), user.pwd());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(userService.createUser(command)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> result = userService.getUsers().stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUser(@PathVariable("userId") String userId) {
        return userService.getUserByUserId(userId)
                .map(result -> ResponseEntity.ok(UserDetailResponse.from(result)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
