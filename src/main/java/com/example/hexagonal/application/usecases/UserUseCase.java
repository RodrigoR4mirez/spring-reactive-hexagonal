package com.example.hexagonal.application.usecases;

import com.example.hexagonal.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Use case interface for User operations.
 * Defines the input boundary for the application layer.
 */
public interface UserUseCase {

    /**
     * Retrieves a user by their ID.
     *
     * @param id The user ID.
     * @return A Mono containing the user.
     */
    Mono<User> getUserById(Long id);

    /**
     * Updates an existing user's details.
     *
     * @param id The user ID.
     * @param user The user data to update.
     * @return A Mono containing the updated user.
     */
    Mono<User> updateUser(Long id, User user);
}
