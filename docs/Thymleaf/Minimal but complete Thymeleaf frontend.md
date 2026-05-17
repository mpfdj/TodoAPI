Below is a **minimal but complete Thymeleaf frontend** for your Todo app that consumes your existing REST endpoints.

It includes:

*   Spring MVC frontend controller
*   Thymeleaf templates (users, lists, tasks)
*   Forms for CRUD actions

***

# ✅ 1. Add dependency (if missing)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

***

# ✅ 2. Web Controller (Frontend)

Create a controller that calls your REST API using `RestTemplate`:

```java
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
    public String createUser(@RequestParam String name) {
        Map<String, String> body = Map.of("name", name);
        restTemplate.postForObject(BASE_URL + "/users", body, Object.class);
        return "redirect:/ui/users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long userId) {
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
    public String createList(@PathVariable Long userId, @RequestParam String name) {
        Map<String, String> body = Map.of("name", name);
        restTemplate.postForObject(BASE_URL + "/users/" + userId + "/lists", body, Object.class);
        return "redirect:/ui/users/" + userId + "/lists";
    }

    @PostMapping("/users/{userId}/lists/delete")
    public String deleteList(@PathVariable Long userId, @RequestParam Long listId) {
        restTemplate.delete(BASE_URL + "/users/" + userId + "/lists/" + listId);
        return "redirect:/ui/users/" + userId + "/lists";
    }

    // TASKS
    @GetMapping("/users/{userId}/lists/{listId}/tasks")
    public String tasks(@PathVariable Long userId, @PathVariable Long listId, Model model) {
        List tasks = restTemplate.getForObject(
                BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks", List.class);

        model.addAttribute("tasks", tasks);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);
        return "tasks";
    }

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
```

***

# ✅ 3. Thymeleaf Templates

Create folder:

    src/main/resources/templates/

***

## ✅ users.html



***

## ✅ lists.html



***

## ✅ tasks.html



***

# ✅ 4. Run & Access

Start app:

```bash
mvn spring-boot:run
```

Open:

    http://localhost:8080/ui/users

***

# 🚀 Optional Improvements

*   ✅ Add Bootstrap styling
*   ✅ Add checkbox for task completion (PUT endpoint)
*   ✅ Add edit forms
*   ✅ Use `WebClient` instead of `RestTemplate`
*   ✅ Add error handling

***

If you want, I can extend this into:

*   ✅ full modern UI (Bootstrap/Tailwind)
*   ✅ inline editing (no page reload)
*   ✅ login/auth integration with your API
