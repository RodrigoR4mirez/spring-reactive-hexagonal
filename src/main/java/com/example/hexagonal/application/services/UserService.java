package com.example.hexagonal.application.services;

import com.example.hexagonal.application.usecases.UserUseCase;
import com.example.hexagonal.domain.model.User;
import com.example.hexagonal.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service implementation of the UserUseCase.
 * Orchestrates the domain logic and uses ports to interact with the infrastructure.
 * This class is annotated with @Service, making it a Spring Bean, but it only relies on
 * interfaces defined in the domain or application layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * Retrieves a user by ID.
     * <p>
     * Reactive Flow:
     * 1. Calls the port to find the user.
     * 2. Logs the result or error signal.
     * 3. If empty, the downstream operator will handle the 404 (usually in the controller).
     * </p>
     *
     * @param id The user ID.
     * @return Mono<User>
     */
    @Override
    public Mono<User> getUserById(Long id) {
        return userRepositoryPort.findById(id)
                .doOnNext(user -> log.info("User found: {}", user))
                .doOnSubscribe(s -> log.info("Fetching user with id: {}", id));
    }

    /**
     * Updates an existing user.
     * <p>
     * Reactive Flow:
     * 1. Fetch the existing user via the port (findById).
     * 2. If found (flatMap), update the fields with new data.
     * 3. Save the updated user via the port (save).
     * 4. If not found, the Mono completes empty (or we could switchIfEmpty to error).
     * </p>
     *
     * @param id The user ID.
     * @param user The new user data.
     * @return Mono<User>
     */
    @Override
    public Mono<User> updateUser(Long id, User user) {
        return userRepositoryPort.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setEmail(user.getEmail());
                    return userRepositoryPort.save(existingUser);
                })
                .doOnSuccess(u -> log.info("User updated: {}", u))
                .doOnSubscribe(s -> log.info("Updating user with id: {}", id));
    }
}
