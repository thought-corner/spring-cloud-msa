package com.study.userservice.service;

import com.study.userservice.client.OrderServiceClient;
import com.study.userservice.client.dto.OrderResponse;
import com.study.userservice.entity.User;
import com.study.userservice.repository.UserRepository;
import com.study.userservice.service.dto.UserCreateCommand;
import com.study.userservice.service.dto.UserDetailResult;
import com.study.userservice.service.dto.UserResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private static final String ORDER_SERVICE_CIRCUIT_BREAKER = "orderServiceCircuitBreaker";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OrderServiceClient orderServiceClient,
                       CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Transactional
    public UserResult createUser(UserCreateCommand command) {
        User user = new User(command.email(), command.name(), UUID.randomUUID().toString(), passwordEncoder.encode(command.password()));
        userRepository.save(user);
        return UserResult.from(user);
    }

    public Optional<UserDetailResult> getUserByUserId(String userId, String authenticatedUser) {
        return userRepository.findByUserId(userId)
                .map(user -> UserDetailResult.of(user, getOrders(authenticatedUser, userId)));
    }

    public List<UserResult> getUsers() {
        return userRepository.findAll().stream()
                .map(UserResult::from)
                .toList();
    }

    private List<OrderResponse> getOrders(String authenticatedUser, String userId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(ORDER_SERVICE_CIRCUIT_BREAKER);
        return circuitBreaker.run(
                () -> orderServiceClient.getOrders(authenticatedUser, userId),
                throwable -> {
                    log.warn("Failed to load orders of user {}, falling back to an empty list: {}",
                            userId, throwable.getMessage());
                    return new ArrayList<>();
                });
    }
}
