package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.service.TaskService;
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
public class TaskController {

    private final TaskService taskService;


    @RequestMapping(value = "/users/{userId}/lists/{listId}/tasks",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskDTO>> getTasks(@PathVariable("userId") Long userId, @PathVariable("listId") Long listId) {
        List<TaskDTO> tasks = taskService.getTasks(userId, listId);
        if (tasks.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tasks);
    }



    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    // TODO: Implement exceptions here...

}
