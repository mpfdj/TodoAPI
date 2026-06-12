package jaeger.de.miel.TodoAPI.controller.thymeleaf;

import jaeger.de.miel.TodoAPI.dto.*;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.TaskService;
import jaeger.de.miel.TodoAPI.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8080";

    private final UserService userService;
    private final ListService listService;
    private final TaskService taskService;

    public UiController(UserService userService, ListService listService, TaskService taskService) {
        this.userService = userService;
        this.listService = listService;
        this.taskService = taskService;
    }


    // Return an empty response to collapse Task edit form
    @GetMapping("/empty")
    @ResponseBody
    public String empty() {
        return "";
    }


    // USERS
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String users(@RequestParam(required = false) Long userId,
                        Model model) {
        if (userId != null) model.addAttribute("userId", userId);
        model.addAttribute("users", userService.getUsers());
        return "users";
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createUser(@RequestParam String email,
                             @RequestParam String name,
                             @RequestParam String password) {

        CreateUserRequestDTO user = CreateUserRequestDTO.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        userService.createUser(user);

        return "redirect:/ui/users";
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "redirect:/ui/users";
    }


    // LISTS
    @GetMapping("/users/{userId}/lists")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String lists(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);

        UserDTO user = userService.getUser(userId);
        model.addAttribute("name", user.getName());
        model.addAttribute("email", user.getEmail());

        List lists = listService.getLists(userId);
        model.addAttribute("lists", lists);

        return "lists";
    }

    @PostMapping("/users/{userId}/lists")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String createList(@PathVariable Long userId, @RequestParam String name, @RequestParam String description) {
        CreateListRequestDTO list = CreateListRequestDTO.builder()
                .name(name)
                .description(description)
                .build();

        listService.createList(userId, list);

        return "redirect:/ui/users/" + userId + "/lists";
    }

    @DeleteMapping("/users/{userId}/lists/{listId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String deleteList(@PathVariable Long userId, @PathVariable Long listId) {
        listService.deleteList(userId, listId);
        return "redirect:/ui/users/" + userId + "/lists";
    }


    // TASKS
    @GetMapping("/users/{userId}/lists/{listId}/tasks")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String tasks(@PathVariable Long userId, @PathVariable Long listId, Model model) {
        ListDTO list = listService.getList(userId, listId);
        List<TaskDTO> tasks = taskService.getTasks(userId, listId);

        model.addAttribute("listName", list.getName());
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);
        model.addAttribute("tasks", tasks);

        return "tasks";
    }

    @GetMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String task(@PathVariable Long userId,
                       @PathVariable Long listId,
                       @PathVariable Long taskId,
                       Model model) {
        TaskDTO task = taskService.getTask(userId, listId, taskId);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);
        model.addAttribute("task", task);
        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
    }

    @GetMapping("/users/{userId}/lists/{listId}/tasks/{taskId}/edit-form")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String getTaskEditForm(@PathVariable Long userId,
                                  @PathVariable Long listId,
                                  @PathVariable Long taskId,
                                  Model model) {

        TaskDTO task = taskService.getTask(userId, listId, taskId);

        model.addAttribute("task", task);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);

        return "fragments/task-edit-form :: task-edit-form";
    }

    @PostMapping("/users/{userId}/lists/{listId}/tasks")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String createTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
                             @RequestParam Integer priority) {
        CreateTaskRequestDTO task = CreateTaskRequestDTO.builder()
                .title(title)
                .description(description)
                .status(TaskStatus.TODO)
                .dueDate(dueDate)
                .priority(priority)
                .build();

        taskService.createTask(userId, listId, task);

        return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
    }

    @PutMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String updateTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @PathVariable Long taskId,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
                             @RequestParam(required = false) Integer priority,
                             Model model) {

        UpdateTaskRequestDTO task = new UpdateTaskRequestDTO();

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(TaskStatus.toEnum(status));
        if (dueDate != null) task.setDueDate(dueDate);
        if (priority != null) task.setPriority(priority);

        // Update task
        taskService.updateTask(userId, listId, taskId, task);

        // Get updated task
        TaskDTO updatedTask = taskService.getTask(userId, listId, taskId);

        // Add to model
        model.addAttribute("task", updatedTask);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);

        return "fragments/task-item :: task-item";
    }

    @DeleteMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
    public String deleteTask(@PathVariable Long userId,
                             @PathVariable Long listId,
                             @PathVariable Long taskId,
                             Model model) {


        taskService.deleteTask(userId, listId, taskId);

        // Fetch updated tasks
        List<TaskDTO> tasks = taskService.getTasks(userId, listId);

        model.addAttribute("tasks", tasks);
        model.addAttribute("userId", userId);
        model.addAttribute("listId", listId);

        return "fragments/tasks-container :: tasks-container";
    }


}
