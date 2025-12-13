package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
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
        if (lists.isEmpty()) return ResponseEntity.notFound().build();  // 404
        return ResponseEntity.ok(lists);
    }


    @RequestMapping(value = "/users",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserDTO created = userService.createUser(request);

        URI location = URI.create("/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }


    @RequestMapping(value = "/users/{userId}/lists",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListDTO> createList(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody CreateListRequestDTO request) {

        ListDTO created = listService.createList(userId, request);
        long listId = created.getId();

        URI location = URI.create("/users/" + userId + "/lists" + listId);
        return ResponseEntity.created(location).body(created);
    }



    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    @ExceptionHandler(ListService.OwnerNotFoundException.class)
    public ResponseEntity<String> handleOwnerNotFound(ListService.OwnerNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ListService.DuplicateListNameException.class)
    public ResponseEntity<String> handleDuplicateListName(ListService.DuplicateListNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

}
