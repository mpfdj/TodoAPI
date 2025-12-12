package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.TaskService;
import jaeger.de.miel.TodoAPI.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

}
