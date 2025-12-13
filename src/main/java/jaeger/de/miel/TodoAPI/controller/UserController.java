package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@Controller
public class UserController {

    private final UserService userService;


    @RequestMapping(value = "/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getUsers();
        if (users.isEmpty()) return ResponseEntity.notFound().build();  // 404
        return ResponseEntity.ok(users);
    }


    @RequestMapping(value = "/users",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserDTO created = userService.createUser(request);

        URI location = URI.create("/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUserByEmail(@RequestParam("email") String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    @ExceptionHandler(UserService.DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(UserService.DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserService.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserService.UserNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

}
