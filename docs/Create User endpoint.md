Absolutely! Below is a clean, production‑friendly example of a **Spring Boot** REST endpoint that adds a new user. It includes:

*   A `User` JPA entity
*   A `UserDTO` for input validation
*   A `UserRepository`
*   A `UserService` with basic business logic (incl. unique email check)
*   A `UserController` with `@PostMapping` that returns `201 Created` and a `Location` header
*   Basic validation and error handling

> **Note:** This example assumes you already have Spring Web, Spring Data JPA, and a database configured (H2/PostgreSQL/etc.). It uses bean validation (`jakarta.validation`) and illustrates good REST practices.

***

## 1) User Entity

```java
package com.example.users.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or SEQUENCE for PostgreSQL
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {} // JPA

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // getters and setters omitted for brevity
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
```

***

## 2) DTO for Create Request

```java
package com.example.users.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 180)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    // getters and setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

***

## 3) DTO for Response

```java
package com.example.users.api.dto;

import java.time.Instant;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Instant createdAt;

    public UserResponse(Long id, String fullName, String email, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    // getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
}
```

***

## 4) Repository

```java
package com.example.users.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
```

***

## 5) Service (with basic password hashing & duplicate email check)

```java
package com.example.users.service;

import com.example.users.api.dto.CreateUserRequest;
import com.example.users.domain.User;
import com.example.users.domain.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // or inject via @Bean
    }

    @Transactional
    public User createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new DuplicateEmailException("Email already in use: " + req.getEmail());
        }

        String normalizedEmail = req.getEmail().trim().toLowerCase();
        String hash = passwordEncoder.encode(req.getPassword());

        User user = new User(req.getFullName().trim(), normalizedEmail, hash);
        return userRepository.save(user);
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }
}
```

> If you’re not using Spring Security yet, you can still use `BCryptPasswordEncoder` by adding the dependency. For real apps, consider injecting the encoder as a bean and applying stronger password policies.

***

## 6) Controller (REST endpoint to add a new user)

```java
package com.example.users.api;

import com.example.users.api.dto.CreateUserRequest;
import com.example.users.api.dto.UserResponse;
import com.example.users.domain.User;
import com.example.users.service.UserService;
import com.example.users.service.UserService.DuplicateEmailException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.createUser(request);

        UserResponse body = new UserResponse(
            created.getId(),
            created.getFullName(),
            created.getEmail(),
            created.getCreatedAt()
        );

        // Return 201 Created with Location: /api/users/{id}
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location).body(body);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(409).body(ex.getMessage()); // 409 Conflict
    }
}
```

***

## 7) Example `curl` Request

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Miel de Jaeger",
        "email": "miel@example.com",
        "password": "StrongP@ssw0rd!"
      }'
```

**Expected response**:

*   Status: `201 Created`
*   Header: `Location: /api/users/{id}`
*   Body (JSON):

```json
{
  "id": 1,
  "fullName": "Miel de Jaeger",
  "email": "miel@example.com",
  "createdAt": "2025-12-12T19:17:38Z"
}
```

***

## 8) Optional: Security & Validation Notes

*   **Never store plaintext passwords**; use a strong hashing algorithm like **BCrypt** (as above), Argon2, or PBKDF2.
*   Consider **rate limiting** and **CAPTCHA** if the endpoint is public.
*   Add **strong password policy** and **email verification** flows.
*   Return **generic error messages** for security (avoid revealing which field failed too precisely in public APIs).
*   For larger projects, prefer **DTOs** and **Mapper** classes (e.g., MapStruct) to separate API and domain models.

***

If you want, I can adapt this to your existing project structure (e.g., package names you’ve been using like `jaeger.de.miel.TodoAPI.*`), or swap H2/ Postgres configurations, or add integration tests with `@SpringBootTest` and `MockMvc`.
