package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
@RequestMapping("/ui")
public class TaskUIController {

    @Autowired
    private RestTemplate restTemplate;
    
    private static final String API_BASE_URL = "http://localhost:8080";
    
    @GetMapping("/users/{userId}/lists/{listId}/tasks")
    public String listTasks(@PathVariable Long userId, @PathVariable Long listId, Model model) {
        try {
            // Fetch list info for display
            String listUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId;
            ResponseEntity<Map<String, Object>> listResponse = restTemplate.getForEntity(listUrl,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (listResponse.getStatusCode() == HttpStatus.OK && listResponse.getBody() != null) {
                model.addAttribute("listName", listResponse.getBody().get("name"));
            } else {
                model.addAttribute("listName", "Unknown List");
            }
            
            // Fetch tasks for this list
            String tasksUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks";
            ResponseEntity<List<Map<String, Object>>> tasksResponse = restTemplate.exchange(
                tasksUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            if (tasksResponse.getStatusCode() == HttpStatus.OK && tasksResponse.getBody() != null) {
                model.addAttribute("tasks", tasksResponse.getBody());
            } else {
                model.addAttribute("tasks", Collections.emptyList());
            }
            
            model.addAttribute("userId", userId);
            model.addAttribute("listId", listId);
            
            return "tasks";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load tasks: " + e.getMessage());
            model.addAttribute("tasks", Collections.emptyList());
            model.addAttribute("userId", userId);
            model.addAttribute("listId", listId);
            model.addAttribute("listName", "List");
            return "tasks";
        }
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
    
    @PutMapping("/users/{userId}/lists/{listId}/tasks/{taskId}/toggle")
    public String toggleTask(@PathVariable Long userId, @PathVariable Long listId, 
                            @PathVariable Long taskId) {
        try {
            // First get the current task
            String getUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
            ResponseEntity<Map<String, Object>> taskResponse = restTemplate.getForEntity(getUrl,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (taskResponse.getStatusCode() == HttpStatus.OK && taskResponse.getBody() != null) {
                Map<String, Object> task = taskResponse.getBody();
                boolean currentStatus = (boolean) task.getOrDefault("completed", false);
                
                // Update the task with toggled status
                task.put("completed", !currentStatus);
                
                String updateUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(task, headers);
                
                restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
                );
            }
            
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
            
        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=" + e.getMessage();
        }
    }
    
    @DeleteMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    public String deleteTask(@PathVariable Long userId, @PathVariable Long listId, 
                            @PathVariable Long taskId) {
        try {
            String url = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
            restTemplate.delete(url);
            
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
            
        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/users/{userId}/lists/{listId}/tasks/{taskId}/edit")
    public String editTaskForm(@PathVariable Long userId, @PathVariable Long listId, 
                              @PathVariable Long taskId, Model model) {
        try {
            String url = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                model.addAttribute("task", response.getBody());
                model.addAttribute("userId", userId);
                model.addAttribute("listId", listId);
            }
            
            return "edit-task";
            
        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=Failed to load task for editing";
        }
    }
    
    @PutMapping("/users/{userId}/lists/{listId}/tasks/{taskId}")
    public String updateTask(@PathVariable Long userId, @PathVariable Long listId, 
                            @PathVariable Long taskId, @RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) Boolean completed) {
        try {
            String url = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", taskId);
            requestBody.put("title", title);
            requestBody.put("description", description != null ? description : "");
            requestBody.put("completed", completed != null ? completed : false);
            requestBody.put("listId", listId);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Make PUT request
            restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
            
        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=" + e.getMessage();
        }
    }
    
    @DeleteMapping("/users/{userId}/lists/{listId}/tasks/delete-all")
    public String deleteAllTasks(@PathVariable Long userId, @PathVariable Long listId) {
        try {
            // First get all tasks
            String getTasksUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks";
            ResponseEntity<List<Map<String, Object>>> tasksResponse = restTemplate.exchange(
                getTasksUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            if (tasksResponse.getStatusCode() == HttpStatus.OK && tasksResponse.getBody() != null) {
                // Delete each task individually
                for (Map<String, Object> task : tasksResponse.getBody()) {
                    Long taskId = ((Number) task.get("id")).longValue();
                    String deleteUrl = API_BASE_URL + "/users/" + userId + "/lists/" + listId + "/tasks/" + taskId;
                    restTemplate.delete(deleteUrl);
                }
            }
            
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks";
            
        } catch (Exception e) {
            return "redirect:/ui/users/" + userId + "/lists/" + listId + "/tasks?error=" + e.getMessage();
        }
    }
}