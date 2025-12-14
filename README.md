# 📘 Spring Boot Reactive Hexagonal Architecture - Deep Dive

Este proyecto es una implementación de referencia de una API RESTful utilizando **Spring Boot 3**, **Arquitectura Hexagonal**, **Programación Reactiva (Project Reactor)** y desarrollo **Contract First** con OpenAPI.

A continuación, se detalla **por qué** se tomó cada decisión, **qué** hace cada clase y método, y **cómo** funciona el flujo reactivo paso a paso.

---

## 🏗 Arquitectura y Decisiones de Diseño

### 1. Arquitectura Hexagonal (Ports & Adapters)
**¿Por qué?**
Para desacoplar la lógica de negocio (Dominio) de los detalles técnicos (Base de datos, Framework Web, UI). Esto permite cambiar la base de datos o el framework sin tocar las reglas de negocio, y facilita el testing unitario.

*   **Dominio (`com.example.hexagonal.domain`)**: El núcleo. Contiene los modelos de negocio y las interfaces (puertos) que definen qué necesita el dominio para funcionar. No tiene dependencias de Spring (excepto Reactor para el flujo asíncrono).
*   **Aplicación (`com.example.hexagonal.application`)**: Orquesta los casos de uso. Conecta los puertos de entrada (UI/API) con los puertos de salida (Persistencia).
*   **Infraestructura (`com.example.hexagonal.infrastructure`)**: La implementación técnica. Contiene los Controladores REST (Adaptadores de Entrada) y los Repositorios de Base de Datos (Adaptadores de Salida).

### 2. Programación Reactiva (WebFlux & R2DBC)
**¿Por qué?**
Para manejar una alta concurrencia con pocos recursos (hilos). A diferencia del modelo tradicional (un hilo por petición), el modelo reactivo es **no bloqueante**. Si la base de datos tarda, el hilo se libera para atender otras peticiones mientras espera la respuesta.

### 3. Contract First (OpenAPI)
**¿Por qué?**
Definimos la API *antes* de programar (`src/main/resources/openapi.yaml`). Esto asegura que el cliente y el servidor estén de acuerdo en el contrato. Las interfaces Java se **generan automáticamente**, evitando errores humanos al escribir controladores.

---

## 🔍 Detalle Clase por Clase

### 📦 Capa de Dominio (Domain)
Esta capa es "pura". No sabe que existe HTTP ni SQL.

#### 1. `User.java` (Modelo)
*   **Qué es**: Un POJO (Plain Old Java Object) que representa un Usuario en el negocio.
*   **Por qué así**: No tiene anotaciones de base de datos (`@Table`, `@Column`) ni de JSON (`@JsonProperty`). Esto nos permite cambiar la DB o la API sin afectar la lógica de negocio.

#### 2. `UserRepositoryPort.java` (Puerto de Salida)
*   **Qué es**: Una interfaz que define las operaciones que el dominio necesita hacer con los datos (buscar, guardar).
*   **Método `Mono<User> findById(Long id)`**: Devuelve un `Mono`. En reactivo, `Mono` es una promesa de que *en el futuro* habrá 0 o 1 Usuario.
*   **Por qué así**: El dominio dice *"Necesito guardar un usuario"*, pero no le importa si es en H2, MySQL o un archivo de texto. La implementación se inyectará después.

---

### 📦 Capa de Aplicación (Application)
La capa que coordina.

#### 3. `UserUseCase.java` (Puerto de Entrada)
*   **Qué es**: Interfaz que define qué operaciones de negocio ofrece el sistema al mundo exterior.
*   **Por qué así**: Define el límite de lo que la aplicación *sabe hacer*.

#### 4. `UserService.java` (Servicio de Aplicación)
*   **Qué es**: La implementación de la lógica. Implementa `UserUseCase`.
*   **Por qué así**: Aquí es donde ocurre la "magia" reactiva.

**Análisis del método `updateUser`:**
```java
public Mono<User> updateUser(Long id, User user) {
    return userRepositoryPort.findById(id) // 1. Buscamos el usuario (Asíncrono)
            .flatMap(existingUser -> {     // 2. Solo si existe, ejecutamos esto:
                existingUser.setFirstName(user.getFirstName()); // Actualizamos lógica
                return userRepositoryPort.save(existingUser);   // 3. Guardamos (Asíncrono)
            })
            .doOnSuccess(...) // 4. Log cuando todo termine bien
            .doOnSubscribe(...); // 0. Log justo cuando alguien se suscribe
}
```
*   **El flujo**:
    1.  **`findById(id)`**: Lanza una petición a la DB. Retorna un `Mono<User>`.
    2.  **`.flatMap(...)`**: Es el operador clave. Significa *"Cuando llegue el usuario, úsalo para crear otra operación asíncrona (guardar)"*. Si usáramos `.map`, tendríamos un `Mono<Mono<User>>` (anidado), lo cual es malo. `flatMap` lo aplana.
    3.  **Si no existe**: Si `findById` retorna vacío (Empty), el `flatMap` **nunca se ejecuta** y el flujo termina vacío. Esto maneja el caso 404 implícitamente.

---

### 📦 Capa de Infraestructura (Infrastructure)

#### 5. `UserEntity.java` (Entidad de Persistencia)
*   **Qué es**: El espejo de la tabla `users` en la base de datos H2. Tiene anotaciones como `@Id` y `@Table`.
*   **Por qué así**: Separamos `User` (Dominio) de `UserEntity` (Infraestructura) para que cambios en la DB no rompan el negocio.

#### 6. `R2dbcUserRepository.java`
*   **Qué es**: Interfaz mágica de Spring Data R2DBC.
*   **Por qué así**: Spring genera el SQL (SELECT, INSERT) automáticamente en tiempo de ejecución.

#### 7. `UserPersistenceAdapter.java` (Adaptador de Salida)
*   **Qué es**: El puente. Implementa `UserRepositoryPort` (del dominio) pero usa `R2dbcUserRepository` (de infraestructura).
*   **Función Clave**: Convertir `User` a `UserEntity` y viceversa (Mappers).
*   **Por qué así**: El dominio no sabe de `UserEntity`. El adaptador hace la traducción.

#### 8. `UserRestController.java` (Adaptador de Entrada)
*   **Qué es**: El controlador Web. Implementa la interfaz `UsersApi` generada por OpenAPI.
*   **Método `getUserById`**:
    ```java
    public Mono<ResponseEntity<UserResponse>> getUserById(Long id, ...) {
        return userUseCase.getUserById(id)       // 1. Llama al servicio
                .map(this::toResponse)           // 2. Convierte Dominio -> DTO
                .map(ResponseEntity::ok)         // 3. Envuelve en HTTP 200 OK
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())); // 4. Si está vacío, devuelve 404
    }
    ```
*   **Por qué así**: El controlador solo se preocupa de HTTP (códigos de estado, JSON). Delega toda la lógica al caso de uso.

---

## 🌊 El Flujo Reactivo: De Principio a Fin

Imaginemos que haces una petición `GET /users/1`. ¿Qué pasa exactamente?

### 1. La Suscripción (El detonante)
En programación reactiva, **nada pasa hasta que alguien se suscribe**.
1.  Llega la petición HTTP a Netty (el servidor embebido).
2.  Spring WebFlux enruta la petición a `UserRestController.getUserById`.
3.  El controlador llama a `UserService`.
4.  El servicio llama a `UserPersistenceAdapter`.
5.  El adaptador llama a `R2dbcUserRepository`.
6.  **Aquí empieza el retorno**: El repositorio devuelve un `Mono` (una tubería vacía).
7.  El adaptador conecta su tubería a la del repositorio.
8.  El servicio conecta su tubería a la del adaptador.
9.  El controlador conecta su tubería a la del servicio.
10. Finalmente, **WebFlux se suscribe** al `Mono` final que devolvió el controlador.
    *   *¡Click!* Al suscribirse, la "corriente" empieza a fluir hacia arriba (hacia la DB).

### 2. La Ejecución (Stream Downstream)
Una vez suscrito, los datos fluyen desde la fuente (DB) hacia abajo (Cliente):

1.  **DB (H2)**: Ejecuta `SELECT * FROM users WHERE id=1`. Encuentra el registro.
2.  **R2DBC**: Emite un evento `onNext(Row)` con los datos crudos.
3.  **R2dbcUserRepository**: Convierte la Row a `UserEntity` y la pasa abajo.
4.  **UserPersistenceAdapter**: Recibe `UserEntity`. Ejecuta `.map(toDomain)`. Transforma `UserEntity` -> `User`. Pasa el `User` abajo.
5.  **UserService**:
    *   Ejecuta `.doOnNext(...)`: Loguea "User found: ...".
    *   Pasa el `User` abajo.
6.  **UserRestController**:
    *   Recibe el `User`.
    *   Ejecuta `.map(toResponse)`: Transforma `User` -> `UserResponse` (DTO).
    *   Ejecuta `.map(ResponseEntity::ok)`: Envuelve el DTO en un objeto HTTP con status 200.
7.  **WebFlux Framework**:
    *   Recibe el `ResponseEntity`.
    *   Serializa el objeto a JSON (`{"id": 1, ...}`).
    *   Escribe la respuesta en el socket HTTP y cierra la conexión.
    *   Envía la señal `onComplete()`.

---

## 🚀 Cómo Ejecutar el Proyecto

Este proyecto usa generación de código. Si abres el IDE y ves errores en rojo, es normal antes de compilar.

### Paso 1: Generar las clases de OpenAPI
Maven leerá `openapi.yaml` y creará las interfaces y DTOs.
```bash
mvn clean install
```
*Las clases generadas estarán en: `target/generated-sources/openapi`*

### Paso 2: Ejecutar
```bash
mvn spring-boot:run
```

### Paso 3: Probar
Ve a Swagger UI para ver la documentación interactiva:
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🧪 Testing

Los tests de integración (`IntegrationTest.java`) levantan el contexto completo de Spring.
*   Usan `WebTestClient`: Un cliente reactivo para probar endpoints.
*   Usan `@DirtiesContext`: Para limpiar la base de datos en memoria después de cada test y asegurar que un test no afecte a otro.

```bash
mvn test
```
