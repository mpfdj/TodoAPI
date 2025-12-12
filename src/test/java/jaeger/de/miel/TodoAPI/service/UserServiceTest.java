package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void createUserIfEmailNotExists() {
        CreateUserRequestDTO request = createUserRequestDTO();

        userRepository.deleteByEmail(request.getEmail());

        UserDTO userCreated = userService.createUser(request);
        System.out.println(userCreated);

        assertTrue(userCreated.getId() > 0);
        assertEquals("mpf.dejaeger@gmail.com", userCreated.getEmail());
        assertEquals("Miel de Jaeger", userCreated.getName());
    }


    @Test
    public void createUserIfEmailExists() {
        CreateUserRequestDTO request = createUserRequestDTO();

        userRepository.deleteByEmail(request.getEmail());
        userService.createUser(request);

        try {
            userService.createUser(request);
        } catch (Exception e) {
            assertEquals(UserService.DuplicateEmailException.class, e.getClass());
            assertEquals("Email already in use: mpf.dejaeger@gmail.com", e.getMessage());
        }
    }


    public CreateUserRequestDTO createUserRequestDTO() {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO();
        createUserRequestDTO.setEmail("mpf.dejaeger@gmail.com");
        createUserRequestDTO.setName("Miel de Jaeger");
        createUserRequestDTO.setPassword("password");
        return createUserRequestDTO;
    }
}