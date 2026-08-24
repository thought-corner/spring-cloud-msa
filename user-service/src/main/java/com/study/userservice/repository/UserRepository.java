package com.study.userservice.repository;

import com.study.userservice.entity.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {

	Optional<User> findByUserId(String userId);

	Optional<User> findByEmail(String email);
}
