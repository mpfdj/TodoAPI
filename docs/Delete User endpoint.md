Got it! Here’s a **clean, production‑friendly** Spring Boot example for a **Delete User** endpoint.  
It follows good REST practices:

*   **`DELETE /api/users/{id}`** → returns **`204 No Content`** on success
*   **`404 Not Found`** if the user doesn’t exist
*   Uses **service-layer exceptions** and **Problem Details** for consistent error responses
*   Transactional and repository-safe handling (covers `EmptyResultDataAccessException`)
*   Includes an optional **delete by email** endpoint if that fits your API

> I’ll use your package style (`jaeger.de.miel.TodoAPI.*`).

***

## 1) Repository

```java
package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    void deleteByEmail(String email);
    boolean existsByEmail(String email);
}
```

> If you prefer JPQL for delete-by-email:
>
> ```java
> @Modifying
> @Query("DELETE FROM AppUser u WHERE u.email = :email")
> int deleteByEmailReturningCount(@Param("email") String email);
> ```

***

## 2) Service (transactional, domain exception)

```java
package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    /** Delete by ID. Throws NotFound if the entity is absent. */
    @Transactional
    public void deleteUserById(long userId) {
        try {
            userRepository.deleteById(userId);
            // deleteById issues a select first in newer JPA impls; if row is missing, it may throw EmptyResultDataAccessException
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found: id=" + userId, e);
        }
    }

    /** Idempotent delete option: do nothing if missing (no exception). */
    @Transactional
    public void deleteUserByIdIdempotent(long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        }
    }

    /** Delete by email with clear not-found semantics. */
    @Transactional
    public void deleteUserByEmail(String email) {
        var user = userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new UserNotFoundException("User not found: email=" + email));
        userRepository.delete(user);
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    // Domain exception kept near the service for clarity
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
        public UserNotFoundException(String message, Throwable cause) { super(message, cause); }
    }
}
```

> **Choose one** of the two delete-by-id behaviors:
>
> *   **Strict** (above): 404 when missing (common in resource APIs).
> *   **Idempotent**: silently succeed with `204` even if it didn’t exist (also acceptable).

***

## 3) Controller (REST, Problem Details)

```java
package jaeger.de.miel.TodoAPI.api;

import jaeger.de.miel.TodoAPI.service.UserService;
import jaeger.de.miel.TodoAPI.service.UserService.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /** DELETE /api/users/{id} → 204 on success, 404 if not found */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") long id) {
        userService.deleteUserById(id); // or deleteUserByIdIdempotent(id)
        return ResponseEntity.noContent().build(); // 204
    }

    /** Optional: DELETE by email (if you expose such an endpoint) */
    @DeleteMapping
    public ResponseEntity<Void> deleteUserByEmail(@RequestParam("email") String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // ---- Local exception mapping; alternatively use @RestControllerAdvice globally ----
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(UserNotFoundException ex, javax.servlet.http.HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("User not found");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://example.com/problems/user-not-found"));
        pd.setInstance(URI.create(req.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
}
```

***

## 4) Entity (for context)

```java
package jaeger.de.miel.TodoAPI.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "APP_USER", uniqueConstraints = {
        @UniqueConstraint(name = "UK_APP_USER_EMAIL", columnNames = "EMAIL")
})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="NAME", nullable=false, length=100)
    private String name;

    @Column(name="EMAIL", nullable=false, length=180)
    private String email;

    @Column(name="PASSWORD_HASH", nullable=false, length=100)
    private String passwordHash;

    @Column(name="CREATED_AT", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="UPDATED_AT", nullable=false)
    private Instant updatedAt;

    // getters/setters omitted for brevity
}
```

***

## 5) Behavior & Status Codes (quick guide)

*   **200 OK** — You typically don’t return a body for delete; prefer 204.
*   **204 No Content** — **Recommended** on success.
*   **404 Not Found** — If you choose strict semantics and the user is not found.
*   **401/403** — If your API requires auth/permissions and user isn’t authorized.

***

## 6) cURL Examples

**Delete by ID:**

```bash
curl -i -X DELETE "http://localhost:8080/api/users/123"
# → 204 No Content  (or 404 if not found with the strict variant)
```

**Delete by email (optional endpoint):**

```bash
curl -i -X DELETE "http://localhost:8080/api/users?email=miel@example.com"
# → 204 No Content
```

***

## 7) Optional: Global exception handler

If you want to centralize errors:

```java
package jaeger.de.miel.TodoAPI.api;

import jaeger.de.miel.TodoAPI.service.UserService.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleNotFound(UserNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("User not found");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://example.com/problems/user-not-found"));
        return pd;
    }
}
```

***

## Notes & Best Practices

*   Keep deletion **idempotent** if clients may retry—returning `204` regardless. If clients rely on 404 for UX, keep **strict** semantics.
*   Avoid exposing **delete-by-email** publicly unless you enforce strong authorization; consider using IDs for canonical deletes.
*   Consider **soft deletes** (flag + filters) if you need recovery/auditing; otherwise hard delete is fine.
*   In tests, if you want to see changes in H2 console, don’t run test methods with a rollback transaction; use `@Commit`.

***

If you share whether you want **strict vs idempotent delete** and whether you prefer **delete by ID only**, I can tailor the service/controller to your exact preference and plug it into your existing `TodoAPI` project structure.
