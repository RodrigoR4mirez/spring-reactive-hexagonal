package com.example.hexagonal.infrastructure.adapters.output.persistence;

import com.example.hexagonal.domain.model.User;
import com.example.hexagonal.domain.ports.UserRepositoryPort;
import com.example.hexagonal.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.example.hexagonal.infrastructure.adapters.output.persistence.repository.R2dbcUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Persistence Adapter (Output Adapter).
 * Implements the UserRepositoryPort to bridge the Domain layer with the R2DBC Infrastructure.
 * Translates between Domain Models (User) and Database Entities (UserEntity).
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final R2dbcUserRepository repository;

    @Override
    public Mono<User> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        UserEntity entity = toEntity(user);
        return repository.save(entity)
                .map(this::toDomain);
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .build();
    }

    private UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId()) // Can be null for new entities
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}
