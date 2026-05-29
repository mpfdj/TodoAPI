
package jaeger.de.miel.TodoAPI.controller.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jaeger.de.miel.TodoAPI.dto.*;
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
public class ListController {

    private final ListService listService;


    @GetMapping(value = "/users/{userId}/lists", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "Lists found")
    @ApiResponse(responseCode = "404", description = "No lists found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<List<ListDTO>> getLists(@PathVariable("userId") Long userId) {
        List<ListDTO> lists = listService.getLists(userId);
        if (lists.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(lists);
    }


    @GetMapping(value = "/users/{userId}/lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "List found")
    @ApiResponse(responseCode = "404", description = "List not found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<?> getList(@PathVariable("userId") Long userId,
                                     @PathVariable("listId") Long listId) {
        try {
            ListDTO listDTO = listService.getList(userId, listId);
            URI location = URI.create("/users/" + userId + "/lists/" + listId);
            return ResponseEntity.status(HttpStatus.OK).location(location).body(listDTO);
        } catch (ListService.ListNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PostMapping(value = "/users/{userId}/lists", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "List created")
    @ApiResponse(responseCode = "404", description = "Owner not found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate list name", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
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


    @DeleteMapping(value = "/users/{userId}/lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "204", description = "List deleted")
    @ApiResponse(responseCode = "404", description = "List not found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
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


    @PutMapping(value = "/users/{userId}/lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "List updated")
    @ApiResponse(responseCode = "404", description = "List not found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
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
