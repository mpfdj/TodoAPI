package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8080";

    // USERS
    @GetMapping("/users")
    public String users(Model model) {
        List users = restTemplate.getForObject(BASE_URL + "/users", List.class);
        model.addAttribute("users", users);
        return "users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String email,
                             @RequestParam String name,
                             @RequestParam String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("name", name);
        body.put("password", password);
        restTemplate.postForObject(BASE_URL + "/users", body, Object.class);
        return "redirect:/ui/users";
    }

    @DeleteMapping("/users/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        restTemplate.delete(BASE_URL + "/users/" + userId);
        return "redirect:/ui/users";
    }


    // LISTS
    @GetMapping("/users/{userId}/lists")
    public String lists(@PathVariable Long userId, Model model) {
        List lists = restTemplate.getForObject(BASE_URL + "/users/" + userId + "/lists", List.class);
        model.addAttribute("lists", lists);
        model.addAttribute("userId", userId);
        return "lists";
    }

    @PostMapping("/users/{userId}/lists")
    public String createList(@PathVariable Long userId, @RequestParam String name, @RequestParam String description) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        restTemplate.postForObject(BASE_URL + "/users/" + userId + "/lists", body, Object.class);
        return "redirect:/ui/users/" + userId + "/lists";
    }


    @DeleteMapping("/users/{userId}/lists/{listId}")
    public String deleteList(@PathVariable Long userId, @PathVariable Long listId) {
        restTemplate.delete(BASE_URL + "/users/" + userId + "/lists/" + listId);
        return "redirect:/ui/users/" + userId + "/lists";
    }


    // TASKS
    @GetMapping("/users/{userId}/lists/{listId}/tasks")
    public String tasks(@PathVariable Long userId, @PathVariable Long listId, Model model) {
        List tasks = restTemplate.getForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks", List.class);

        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

    @GetMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    public String task(@PathVariable Long userId,
                       @PathVariable Long listId,
                       @PathVariable Long taskId,
                       Model model) {
        TaskDTO task = restTemplate.getForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId, TaskDTO.class);

        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);
        model.addAttribute("task", task);
        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
    }

    @PostMapping("/users/{userId}/lists/{listId}/tasks")
    public String createTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
                             @RequestParam Integer priority) {
        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description != null ? description : "");
        body.put("status", "todo");
        body.put("dueDate", dueDate.toString());
        body.put("priority", String.valueOf(priority));

        restTemplate.postForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks",
                body,
                Object.class
        );

        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
    }


//      //UPDATE
//    @PutMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
//    public String toggleTask(@PathVariable Long userId,
//                             @PathVariable Long listId,
//                             @PathVariable Long taskId,
//                             @RequestParam(required = false) String title,
//                             @RequestParam(required = false) String description,
//                             @RequestParam(required = false) String status,
//                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
//                             @RequestParam(required = false) Integer priority) {
//
//        Map<String, String> body = new HashMap<>();
//        if (title != null) body.put("title", title);
//        if (description != null) body.put("description", description);
//        if (status != null) body.put("status", status);
//        if (dueDate != null) body.put("dueDate", dueDate.toString());
//        if (priority != null) body.put("priority", String.valueOf(priority));
//
//        restTemplate.put(
//                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId,
//                body,
//                Object.class
//        );
//
//        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
//
//    }

    @PutMapping("/users/{userId}/lists/{listId}/tasks/{taskId}/toggle")
    public String toggleTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @PathVariable Long taskId,
                             @RequestParam String status,
                             Model model) {

        // Update task status
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);

        restTemplate.put(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId,
                body
        );

        // Get updated task
        String getUrl = BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
        Map<String, Object> updatedTask = restTemplate.getForObject(getUrl, Map.class);

        // Add to model
        model.addAttribute("task", updatedTask);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);

        return "fragments/task-item";  // Return ONLY the task fragment, not the whole page
    }

    @DeleteMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @PathVariable Long taskId) {

        System.out.println("========= DEBUG ==========");
        System.out.println("userId: " + userId);
        System.out.println("listId: " + listId);
        System.out.println("taskId: " + taskId);

        restTemplate.delete(BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId);

        return ResponseEntity.ok().build();

    }

}
