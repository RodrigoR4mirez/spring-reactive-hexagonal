# Reactive Hexagonal Spring Boot Demo

This project demonstrates a Spring Boot 3 application using **Hexagonal Architecture**, **Reactive Programming (WebFlux & R2DBC)**, and **Contract First** API development.

## 🚀 How to Run

### 1. Build the Project
This project uses **Contract First** development. The API interfaces and DTOs are **generated** from the `src/main/resources/openapi.yaml` file during the build process.

You **must** run the build to generate these classes before the IDE can recognize them.

```bash
mvn clean install
```

The generated files will be located in:
`target/generated-sources/openapi/src/main/java/`

### 2. Run the Application
```bash
mvn spring-boot:run
```

### 3. Access Swagger UI
Once running, open:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🏗 Project Structure

*   **Domain Layer** (`com.example.hexagonal.domain`):
    *   `model`: Core business entities (`User`).
    *   `ports`: Interfaces for infrastructure (`UserRepositoryPort`).
    *   *No dependencies on Spring or R2DBC.*

*   **Application Layer** (`com.example.hexagonal.application`):
    *   `usecases`: Input boundary interfaces (`UserUseCase`).
    *   `services`: Implementation of business logic (`UserService`).

*   **Infrastructure Layer** (`com.example.hexagonal.infrastructure`):
    *   `adapters.input.rest`: WebFlux REST Controller implementing the **generated** OpenAPI interface.
    *   `adapters.output.persistence`: R2DBC implementation of the repository port.

## 🧪 Testing

Run integration tests:
```bash
mvn test
```

## ❓ FAQ: "Where are the classes?"
If you are looking for `UsersApi`, `UserResponse`, or `UserUpdateRequest`, they are **not** in `src/main/java`. They are generated automatically by the `openapi-generator-maven-plugin` into the `target/` folder when you run Maven. This ensures the implementation strictly follows the API contract defined in `openapi.yaml`.
