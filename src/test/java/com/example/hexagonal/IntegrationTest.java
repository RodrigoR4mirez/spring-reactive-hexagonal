package com.example.hexagonal;

import com.example.hexagonal.infrastructure.adapters.input.rest.dto.UserResponse;
import com.example.hexagonal.infrastructure.adapters.input.rest.dto.UserUpdateRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @Order(1)
    void testGetUserById() {
        // ID 1 is inserted by schema.sql as John Doe
        webTestClient.get().uri("/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(user -> {
                    assertThat(user.getId()).isEqualTo(1L);
                    assertThat(user.getFirstName()).isEqualTo("John");
                });
    }

    @Test
    @Order(2)
    void testUpdateUser() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("JohnUpdated");
        updateRequest.setLastName("DoeUpdated");
        updateRequest.setEmail("john.updated@example.com");

        webTestClient.put().uri("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), UserUpdateRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(user -> {
                    assertThat(user.getId()).isEqualTo(1L);
                    assertThat(user.getFirstName()).isEqualTo("JohnUpdated");
                });
        
        // Verify update
        webTestClient.get().uri("/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(user -> {
                    assertThat(user.getFirstName()).isEqualTo("JohnUpdated");
                });
    }
}
