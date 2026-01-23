package jaeger.de.miel.TodoAPI.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ErrorDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@RestController
@Tag(name = "Users", description = "Operations for managing users")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "List users",
        description = "Returns all users. Responds with **404** if no users are present.",
        // Uncomment if your endpoints require auth and you've defined a security scheme named "bearerAuth"
        // security = @SecurityRequirement(name = "bearerAuth")
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Users found",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "No users found",
                content = @Content // empty body
            )
        }
    )
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getUsers();

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Create a user",
        description = "Creates a new user and returns it. Responds with **409** if the email already exists.",
        requestBody = @RequestBody(
            required = true,
            description = "User details",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreateUserRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created",
                headers = {
                    @Header(
                        name = "Location",
                        description = "URI of the created user resource",
                        schema = @Schema(type = "string", example = "/users/123")
                    )
                },
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate email",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            )
        }
    )
    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateUserRequestDTO request) {
        try {
            UserDTO created = userService.createUser(request);
            URI location = URI.create("/users/" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
        } catch (UserService.DuplicateEmailException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @Operation(
        summary = "Delete user by id",
        description = "Deletes a user by its id. Idempotent: returns **204** even if resource was already removed (but here it returns **404** if not found per current implementation).",
        parameters = {
            @Parameter(
                name = "userId",
                description = "Numeric identifier of the user",
                required = true,
                example = "123"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Deleted"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            )
        }
    )
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserService.UserNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(
        summary = "Delete user by email",
        description = "Deletes a user by email.",
        parameters = {
            @Parameter(
                name = "email",
                description = "Email address of the user to delete",
                required = true,
                example = "jane.doe@example.com"
            )
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid email format",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            )
        }
    )
    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam("email") String email) {
        try {
            userService.deleteUser(email);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UserService.UserNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(
        summary = "Update a user",
        description = "Updates an existing user and returns it.",
        parameters = {
            @Parameter(
                name = "userId",
                description = "Numeric identifier of the user to update",
                required = true,
                example = "123"
            )
        },
        requestBody = @RequestBody(
            required = true,
            description = "Updated user values",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UpdateUserRequestDTO.class)
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User updated",
                headers = {
                    @Header(
                        name = "Location",
                        description = "URI of the updated user resource",
                        schema = @Schema(type = "string", example = "/users/123")
                    )
                },
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Duplicate email",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
                )
            )
        }
    )
    @PutMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(
            @PathVariable("userId") Long userId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateUserRequestDTO request) {
        try {
            UserDTO updated = userService.updateUser(userId, request);
            URI location = URI.create("/users/" + updated.getId());
            return ResponseEntity.status(HttpStatus.OK).location(location).body(updated);
        } catch (UserService.UserNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (UserService.DuplicateEmailException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }


//    // ---------------------------------------
//    // Exceptions
//    // ---------------------------------------
//    @ExceptionHandler(UserService.DuplicateEmailException.class)
//    public ResponseEntity<String> handleDuplicateEmail(UserService.DuplicateEmailException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
//    }
//
//    @ExceptionHandler(UserService.UserNotFoundException.class)
//    public ResponseEntity<String> handleUserNotFound(UserService.UserNotFoundException ex) {
//        return ResponseEntity.notFound().build();
//    }

}
