
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
import jaeger.de.miel.TodoAPI.dto.CreateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ErrorDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.service.TaskService;
import jaeger.de.miel.TodoAPI.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement; // if you add auth

@AllArgsConstructor
@RestController
public class TaskController {

    private final TaskService taskService;


    @GetMapping(value = "/users/{userId}/lists/{listId}/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasks(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId) {

        List<TaskDTO> tasks = taskService.getTasks(userId, listId);
        if (tasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(tasks);
    }


    @PostMapping(value = "/users/{userId}/lists/{listId}/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTask(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateTaskRequestDTO request) {

        try {
            TaskDTO created = taskService.createTask(userId, listId, request);
            long taskId = created.getId();
            URI location = URI.create("/users/" + userId + "/lists/" + listId + "/tasks/" + taskId);
            return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
        } catch (TaskService.CreatorNotFoundException | TaskService.ListNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @DeleteMapping(value = "/users/{userId}/lists/{listId}/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTask(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId) {

        try {
            taskService.deleteTask(userId, listId, taskId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (TaskService.TaskNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PutMapping(value = "/users/{userId}/lists/{listId}/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTask(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateTaskRequestDTO request) {
        try {
            TaskDTO updated = taskService.updateTask(taskId, userId, listId, request);
            URI location = URI.create("/users/" + updated.getUserId() + "/lists/" + updated.getListId() + "/tasks/" + updated.getId());
            return ResponseEntity.status(HttpStatus.OK).location(location).body(updated);
        } catch (TaskService.TaskNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping(value = "/users/{userId}/lists/{listId}/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTasks(
            @PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId) {

        try {
            TaskDTO taskDTO = taskService.getTask(userId, listId, taskId);
            URI location = URI.create("/users/" + taskDTO.getUserId() + "/lists/" + taskDTO.getListId() + "/tasks/" + taskDTO.getId());
            return ResponseEntity.status(HttpStatus.OK).location(location).body(taskDTO);
        } catch (TaskService.TaskNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    // TODO: Implement exceptions here...

    // You can add @ExceptionHandler methods here if you centralize error handling.
}
