package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.*;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@Controller
public class TaskController {

    private final TaskService taskService;


    @RequestMapping(value = "/users/{userId}/lists/{listId}/tasks",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasks(@PathVariable("userId") Long userId, @PathVariable("listId") Long listId) {
        List<TaskDTO> tasks = taskService.getTasks(userId, listId);

        if (tasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(tasks);
    }

    @RequestMapping(value = "/users/{userId}/lists/{listId}/tasks",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTask(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @Valid @RequestBody CreateTaskRequestDTO request) {

        try {
            TaskDTO created = taskService.createTask(userId, listId, request);
            long taskId = created.getId();

            URI location = URI.create("/users/" + userId + "/lists" + listId + "/tasks" + taskId);
            return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
        } catch (TaskService.CreatorNotFoundException | TaskService.ListNotFoundException ex ) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @RequestMapping(value = "/users/{userId}/lists/{listId}/tasks/{taskId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTask(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId) {

        try {
            taskService.deleteTask(userId, listId, taskId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (TaskService.TaskNotFoundException ex ) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    // TODO: Implement exceptions here...

}
