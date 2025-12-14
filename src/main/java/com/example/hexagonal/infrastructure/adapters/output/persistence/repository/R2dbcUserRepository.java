package com.example.hexagonal.infrastructure.adapters.output.persistence.repository;

import com.example.hexagonal.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data R2DBC repository for UserEntity.
 * Provides reactive CRUD operations out of the box.
 */
@Repository
public interface R2dbcUserRepository extends R2dbcRepository<UserEntity, Long> {
}
