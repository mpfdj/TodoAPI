package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.TaskService;
import jaeger.de.miel.TodoAPI.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@Controller
public class IndexController {

    private final UserService userService;
    private final ListService listService;
    private final TaskService taskService;


    @RequestMapping(value = "/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getUsers();
        if (users.isEmpty()) return ResponseEntity.notFound().build();  // 404
        return ResponseEntity.ok(users);
    }


    @RequestMapping(value = "/users/{userId}/lists",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListDTO>> getLists(@PathVariable("userId") Long userId) {
        List<ListDTO> lists = listService.getLists(userId);
        if (lists.isEmpty()) return ResponseEntity.notFound().build();  // 404
        return ResponseEntity.ok(lists);
    }


    @RequestMapping(value = "/users/{userId}/lists/{listId}/tasks",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasks(@PathVariable("userId") Long userId, @PathVariable("listId") Long listId) {
        List<TaskDTO> tasks = taskService.getTasks(userId, listId);
        if (tasks.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tasks);
    }


    @RequestMapping(value = "/user",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserDTO userDTO = userService.createUser(request);

        URI location = URI.create("/users/" + userDTO.getId());
        return ResponseEntity.created(location).body(userDTO);
    }


    @ExceptionHandler(UserService.DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(UserService.DuplicateEmailException ex) {
        return ResponseEntity.status(409).body(ex.getMessage()); // 409 Conflict
    }

}
