
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
import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ErrorDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateListRequestDTO;
import jaeger.de.miel.TodoAPI.service.ListService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement; // if you add auth

@AllArgsConstructor
@RestController
@Tag(name = "Lists", description = "Operations for managing todo lists for a specific user")
public class ListController {

    private final ListService listService;

    @Operation(
            summary = "List all lists for a user",
            description = "Returns all lists that belong to the given user. Responds with **404** if the user has no lists.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "Owner user id",
                            required = true,
                            example = "42"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lists found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = ListDTO.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No lists found for the user",
                            content = @Content // empty body
                    )
            }
    )
    @GetMapping(value = "/users/{userId}/lists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListDTO>> getLists(@PathVariable("userId") Long userId) {
        List<ListDTO> lists = listService.getLists(userId);

        if (lists.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(lists);
    }

    @Operation(
            summary = "Create a list for a user",
            description = "Creates a new list for the specified user and returns it. Responds with **404** if the owner user does not exist; **409** if a list with the same name already exists for the user.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "Owner user id",
                            required = true,
                            example = "42"
                    )
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "List details",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateListRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "List created",
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description = "URI of the created list resource",
                                            schema = @Schema(type = "string", example = "/users/42/lists/1001")
                                    )
                            },
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ListDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Owner user not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Duplicate list name for this user",
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
    @PostMapping(value = "/users/{userId}/lists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createList(
            @PathVariable("userId") Long userId,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateListRequestDTO request) {

        try {
            ListDTO created = listService.createList(userId, request);
            long listId = created.getId();
            URI location = URI.create("/users/" + userId + "/lists/" + listId);
            return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
        } catch (ListService.OwnerNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (ListService.DuplicateListNameException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @Operation(
            summary = "Delete a list",
            description = "Deletes a list by id for the given user.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id", required = true, example = "1001")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "List not found for the given user",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDTO.class)
                            )
                    )
            }
    )
    @DeleteMapping(value = "/users/{userId}/lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteList(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId) {

        try {
            listService.deleteList(userId, listId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ListService.ListNotFoundException ex ) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(
            summary = "Update a list",
            description = "Updates an existing list for the given user and returns it.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id", required = true, example = "1001")
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "Updated list values",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateListRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List updated",
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description = "URI of the updated list resource",
                                            schema = @Schema(type = "string", example = "/users/42/lists/1001")
                                    )
                            },
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ListDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "List not found for the given user",
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
    @PutMapping(value = "/users/{userId}/lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateList(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateListRequestDTO request) {
        try {
            ListDTO updated = listService.updateList(userId, listId, request);
            URI location = URI.create("/users/" + updated.getUserId() + "/lists/" + updated.getId());
            return ResponseEntity.status(HttpStatus.OK).location(location).body(updated);
        } catch (ListService.ListNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


//    // ---------------------------------------
//    // Exceptions
//    // ---------------------------------------
//    @ExceptionHandler(ListService.OwnerNotFoundException.class)
//    public ResponseEntity<String> handleOwnerNotFound(ListService.OwnerNotFoundException ex) {
//        return ResponseEntity.notFound().build();
//    }
//
//    @ExceptionHandler(ListService.DuplicateListNameException.class)
//    public ResponseEntity<String> handleDuplicateListName(ListService.DuplicateListNameException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT).build();
//    }

}
