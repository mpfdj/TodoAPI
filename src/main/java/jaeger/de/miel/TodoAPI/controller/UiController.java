package jaeger.de.miel.TodoAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
//        model.addAttribute("userName", "userName");
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


    @PostMapping("/users/{userId}/lists/{listId}/tasks")
    public String createTask(@PathVariable Long userId, @PathVariable Long listId,
                             @RequestParam String title,
                             @RequestParam(required = false) String description) {
        try {
            String url = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks";

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", title);
            requestBody.put("description", description != null ? description : "");
            requestBody.put("completed", false);
            requestBody.put("listId", listId);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make POST request
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";

        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=" + e.getMessage();
        }
    }


//    TODO: WORK IN PROGRESS


    @PostMapping("/users/{userId}/lists/{listId}/tasks")
    public String createTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @RequestParam String title,
                             @RequestParam(required = false) String description) {

        Map<String, String> body = Map.of("name", name);
        restTemplate.postForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks",
                body,
                Object.class
        );

        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
    }















    // LISTS
//    @GetMapping("/users/{userId}/lists")
//    public String lists(@PathVariable Long userId, Model model) {
//        List lists = restTemplate.getForObject(BASE_URL + "/users/" + userId + "/lists", List.class);
//        model.addAttribute("lists", lists);
//        model.addAttribute("userId", userId);
//        return "lists";
//    }
//
//    @PostMapping("/users/{userId}/lists")
//    public String createList(@PathVariable Long userId, @RequestParam String name) {
//        Map<String, String> body = Map.of("name", name);
//        restTemplate.postForObject(BASE_URL + "/users/" + userId + "/lists", body, Object.class);
//        return "redirect:/ui/users/" + userId + "/lists";
//    }
//
//    @PostMapping("/users/{userId}/lists/delete")
//    public String deleteList(@PathVariable Long userId, @RequestParam Long listId) {
//        restTemplate.delete(BASE_URL + "/users/" + userId + "/lists/" + listId);
//        return "redirect:/ui/users/" + userId + "/lists";
//    }
//
//    // TASKS
//    @GetMapping("/users/{userId}/lists/{listId}/tasks")
//    public String tasks(@PathVariable Long userId, @PathVariable Long listId, Model model) {
//        List tasks = restTemplate.getForObject(
//                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks", List.class);
//
//        model.addAttribute("tasks", tasks);
//        model.addAttribute("userId", userId);
//        model.addAttribute("listId", listId);
//        return "tasks";
//    }

    @PostMapping("/users/{userId}/lists/{listId}/tasks")
    public String createTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @RequestParam String name) {

        Map<String, String> body = Map.of("name", name);
        restTemplate.postForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks",
                body,
                Object.class
        );

        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
    }

    @PostMapping("/users/{userId}/lists/{listId}/tasks/delete")
    public String deleteTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @RequestParam Long taskId) {

        restTemplate.delete(BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId);

        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
    }
}
