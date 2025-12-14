package com.example.hexagonal.domain.ports;

import com.example.hexagonal.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Port for User data access.
 * Defines the contract that the infrastructure layer (persistence) must implement.
 * This allows the domain layer to remain agnostic of the underlying database technology.
 */
public interface UserRepositoryPort {

    /**
     * Finds a user by their ID.
     *
     * @param id The ID of the user.
     * @return A Mono emitting the user if found, or empty.
     */
    Mono<User> findById(Long id);

    /**
     * Saves a user.
     *
     * @param user The user to save.
     * @return A Mono emitting the saved user.
     */
    Mono<User> save(User user);
}
