package jaeger.de.miel.TodoAPI.api;

import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.service.ListService;
import jaeger.de.miel.TodoAPI.service.UserService;
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
public class IndexController {

    private final UserService userService;
    private final ListService listService;

    @RequestMapping(value = "/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getUsers();

        if (users.isEmpty()) return ResponseEntity.notFound().build();  // 404

        return ResponseEntity.ok(users);
    }


    @RequestMapping(value = "/users/{userId}/lists",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListDTO>> getUserLists(@PathVariable("userId") Long userId) {
        List<ListDTO> lists = listService.getLists(userId);

        if (lists.isEmpty()) return ResponseEntity.notFound().build();  // 404

        return ResponseEntity.ok(lists);
    }

}
