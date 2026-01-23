
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
@Tag(name = "Tasks", description = "Operations for managing tasks within a user's list")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "List all tasks for a user's list",
            description = "Returns all tasks belonging to the specified list of the specified user. Responds with **404** if no tasks are present.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id", required = true, example = "1001")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tasks found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No tasks found",
                            content = @Content // empty body
                    )
            }
    )
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

    @Operation(
            summary = "Create a task in a list",
            description = "Creates a new task in the specified list for the specified user and returns it. Responds with **404** if the creator or list does not exist.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id where the task will be created", required = true, example = "1001")
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "Task details",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateTaskRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Task created",
                            headers = {
                                    @Header(
                                            name = "Location",
                                            description = "URI of the created task resource",
                                            schema = @Schema(type = "string", example = "/users/42/lists/1001/tasks/555")
                                    )
                            },
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TaskDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Creator or list not found",
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

    @Operation(
            summary = "Delete a task",
            description = "Deletes a task by id within the specified list for the specified user.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id", required = true, example = "1001"),
                    @Parameter(name = "taskId", description = "Task id", required = true, example = "555")
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found for the given user and list",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorDTO.class)
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Update a task",
            description = "Updates an existing task and returns the updated representation.",
            parameters = {
                    @Parameter(name = "userId", description = "Owner user id", required = true, example = "42"),
                    @Parameter(name = "listId", description = "List id", required = true, example = "1001"),
                    @Parameter(name = "taskId", description = "Task id", required = true, example = "555")
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "Updated task values",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateTaskRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task updated",
                            headers = @Header(
                                    name = "Location",
                                    description = "URI of the updated task resource",
                                    schema = @Schema(type = "string", example = "/users/42/lists/1001/tasks/555")
                            ),
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TaskDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found for the given user and list",
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


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    // TODO: Implement exceptions here...

    // You can add @ExceptionHandler methods here if you centralize error handling.
}
