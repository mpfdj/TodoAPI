If `ErrorDTO` is still not showing in Swagger UI even when defined directly in `openapi.yml`, the issue is likely that **the error responses are not properly referenced in your individual endpoint files** (`users.yml`, `lists.yml`, `tasks.yml`).

## The Problem:

Your `openapi.yml` defines `ErrorDTO`, but your referenced endpoint files (`users.yml`, `lists.yml`, `tasks.yml`) need to **reference** that `ErrorDTO` in their error responses.

## Solution 1: Fix references in `users.yml`

Update your `users.yml` to properly reference `ErrorDTO`:

```yaml
paths:
  /users:
    get:
      tags:
        - Users
      summary: List users
      operationId: getUsers
      responses:
        '200':
          description: Users found
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/UserDTO'
        '404':
          description: No users found
          content: {}

    post:
      tags:
        - Users
      summary: Create a user
      operationId: createUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequestDTO'
      responses:
        '201':
          description: User created
          headers:
            Location:
              schema:
                type: string
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDTO'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference
        '409':
          description: Duplicate email
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference

    delete:
      tags:
        - Users
      summary: Delete user by email
      operationId: deleteUserByEmail
      parameters:
        - name: email
          in: query
          required: true
          schema:
            type: string
      responses:
        '204':
          description: Deleted
        '400':
          description: Invalid email format
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference

  /users/{userId}:
    get:
      tags:
        - Users
      summary: Get user by id
      operationId: getUser
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: User found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDTO'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference

    put:
      tags:
        - Users
      summary: Update a user
      operationId: updateUser
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateUserRequestDTO'
      responses:
        '200':
          description: User updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDTO'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference
        '409':
          description: Duplicate email
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference

    delete:
      tags:
        - Users
      summary: Delete user by id
      operationId: deleteUserById
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: integer
      responses:
        '204':
          description: Deleted
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'  # Fixed reference
```

## Solution 2: Use absolute references to main openapi.yml

If the relative references aren't working, use absolute paths to your main `openapi.yml`:

```yaml
# In users.yml, lists.yml, tasks.yml
responses:
  '400':
    description: Validation error
    content:
      application/json:
        schema:
          $ref: '/openapi.yml#/components/schemas/ErrorDTO'
```

## Solution 3: Define ErrorDTO in each individual file (Temporary fix)

As a temporary workaround, define `ErrorDTO` directly in each endpoint file:

```yaml
# In users.yml, add components section at the bottom
components:
  schemas:
    ErrorDTO:
      type: object
      properties:
        message:
          type: string
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
```

## Solution 4: Check Swagger UI configuration

Make sure your `application.yml` or `application.properties` has the correct configuration:

```yaml
# application.yml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: true
  use-management-port: false
```

## Solution 5: Create a unified YAML file for testing

Create a test file `openapi-test.yml` to verify that ErrorDTO works when not using references:

```yaml
openapi: 3.0.1
info:
  title: TodoAPI - Test
  version: 1.0.0

paths:
  /test:
    get:
      responses:
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorDTO'

components:
  schemas:
    ErrorDTO:
      type: object
      properties:
        message:
          type: string
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
```

If this works, the issue is with the references between files.

## Solution 6: Enable debugging for springdoc

Add this to `application.yml` to see what's happening:

```yaml
logging:
  level:
    org.springdoc: DEBUG
    org.springframework.web: DEBUG
```

## Solution 7: Use @ControllerAdvice for consistent error responses

Instead of manually defining ErrorDTO in OpenAPI, use Spring's `@ControllerAdvice` with `@ExceptionHandler`:

```java
package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex) {
        ErrorDTO error = new ErrorDTO(
            ex.getMessage(),
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Add more specific exception handlers
}
```

Then let springdoc automatically detect the ErrorDTO from your exception handlers.

## Solution 8: Clear browser cache and restart

Sometimes the issue is just browser caching:

1. **Clear your browser cache** (Ctrl+Shift+Delete)
2. **Restart your Spring Boot application**
3. **Access Swagger UI fresh**: `http://localhost:8080/swagger-ui.html`
4. **Check if the OpenAPI JSON shows ErrorDTO**: `http://localhost:8080/v3/api-docs`

## Most Likely Fix:

The issue is that your `users.yml`, `lists.yml`, and `tasks.yml` files are using `$ref: '#/components/schemas/ErrorDTO'` but they don't have access to the components section defined in `openapi.yml` because each file is independent.

**Solution:** Move the `components` section to a separate file and reference it in all files, OR define ErrorDTO in each individual endpoint file.

The quickest fix is to **define ErrorDTO directly in each endpoint YAML file** until you get the references working properly.