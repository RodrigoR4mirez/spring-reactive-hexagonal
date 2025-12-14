package com.example.hexagonal.infrastructure.adapters.input.rest;

import com.example.hexagonal.application.usecases.UserUseCase;
import com.example.hexagonal.domain.model.User;
import com.example.hexagonal.infrastructure.adapters.input.rest.api.UsersApi;
import com.example.hexagonal.infrastructure.adapters.input.rest.dto.UserResponse;
import com.example.hexagonal.infrastructure.adapters.input.rest.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST Controller (Input Adapter).
 * Implements the generated OpenAPI interface (UsersApi).
 * Delegates business logic to UserUseCase.
 * Handles mapping between DTOs and Domain models.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserRestController implements UsersApi {

    private final UserUseCase userUseCase;

    /**
     * GET /users/{id}
     * <p>
     * Reactive Flow:
     * 1. Calls UserUseCase.getUserById(id).
     * 2. Maps the Domain User to UserResponse DTO.
     * 3. Wraps in ResponseEntity.ok().
     * 4. switchIfEmpty: Returns 404 Not Found if the Mono is empty.
     * </p>
     */
    @Override
    public Mono<ResponseEntity<UserResponse>> getUserById(Long id, ServerWebExchange exchange) {
        return userUseCase.getUserById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * PUT /users/{id}
     * <p>
     * Reactive Flow:
     * 1. Maps Request DTO to Domain User.
     * 2. Calls UserUseCase.updateUser(id, user).
     * 3. Maps the returned updated Domain User to UserResponse DTO.
     * 4. Wraps in ResponseEntity.ok().
     * 5. switchIfEmpty: Returns 404 Not Found if the user to update didn't exist.
     * </p>
     */
    @Override
    public Mono<ResponseEntity<UserResponse>> updateUser(Long id, Mono<UserUpdateRequest> userUpdateRequest, ServerWebExchange exchange) {
        return userUpdateRequest
                .map(this::toDomain)
                .flatMap(user -> userUseCase.updateUser(id, user))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // Mappers

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        return response;
    }

    private User toDomain(UserUpdateRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .build();
    }
}
