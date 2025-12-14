package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.*;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@Controller
public class ListController {

    private final UserService userService;
    private final ListService listService;


    @RequestMapping(value = "/users/{userId}/lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListDTO>> getLists(@PathVariable("userId") Long userId) {
        List<ListDTO> lists = listService.getLists(userId);

        if (lists.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(lists);
    }


    @RequestMapping(value = "/users/{userId}/lists",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createList(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody CreateListRequestDTO request) {

        try {
            ListDTO created = listService.createList(userId, request);
            long listId = created.getId();

            URI location = URI.create("/users/" + userId + "/lists" + listId);
            return ResponseEntity.created(location).body(created);
        } catch (ListService.OwnerNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        catch (ListService.DuplicateListNameException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
