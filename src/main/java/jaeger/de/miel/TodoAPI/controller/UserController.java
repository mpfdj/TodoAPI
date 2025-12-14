package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ErrorDTO;
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

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(users);
    }


    @RequestMapping(value = "/users",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        try {
            UserDTO created = userService.createUser(request);
            URI location = URI.create("/users/" + created.getId());
            return ResponseEntity.created(location).body(created);
        } catch (UserService.DuplicateEmailException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserService.UserNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam("email") String email) {
        try {
            userService.deleteUser(email);
            return ResponseEntity.noContent().build();
        } catch (UserService.UserNotFoundException ex) {
            ErrorDTO error = new ErrorDTO(ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


//    // ---------------------------------------
//    // Exceptions
//    // ---------------------------------------
//    @ExceptionHandler(UserService.DuplicateEmailException.class)
//    public ResponseEntity<String> handleDuplicateEmail(UserService.DuplicateEmailException ex) {
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
//    }
//
//    @ExceptionHandler(UserService.UserNotFoundException.class)
//    public ResponseEntity<String> handleUserNotFound(UserService.UserNotFoundException ex) {
//        return ResponseEntity.notFound().build();
//    }

}
