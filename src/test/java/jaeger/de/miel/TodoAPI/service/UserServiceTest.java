package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.mapper.UserMapper;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

//    private final UserRepository userRepository = mock(UserRepository.class);
//    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
//    private final UserService userService = new UserService(userRepository, passwordEncoder);



    @Test
    public void testGetUsers() {
        var u1 = createUser(1L, "zara@mail.com", "Zara");
        var u2 = createUser(2L, "bob@mail.com", "Bob");
        var u3 = createUser(3L, "alice@mail.com", "Alice");
        List<AppUser> users = Arrays.asList(u1, u2, u3);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> result = userService.getUsers();
        var dto1 = result.get(0);
        var dto2 = result.get(1);
        var dto3 = result.get(2);

        assertEquals("Alice", dto1.getName());
        assertEquals("alice@mail.com", dto1.getEmail());
        assertEquals(3L, dto1.getId());

        assertEquals("Bob", dto2.getName());
        assertEquals("bob@mail.com", dto2.getEmail());
        assertEquals(2L, dto2.getId());

        assertEquals("Zara", dto3.getName());
        assertEquals("zara@mail.com", dto3.getEmail());
        assertEquals(1L, dto3.getId());

        // Assert: Sorted by name ascending
        assertThat(result)
                .hasSize(3)
                .extracting(UserDTO::getName)
                .containsExactly("Alice", "Bob", "Zara");
    }


    @Test
    void testCreateUser() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("unittest@mail.com");

        AppUser appUser = new AppUser();
        AppUser savedUser = new AppUser();
        savedUser.setId(1L);

        UserDTO expectedDto = new UserDTO();
        expectedDto.setId(1L);

        when(userRepository.existsByEmail("unittest@mail.com")).thenReturn(false);
        when(userRepository.save(appUser)).thenReturn(savedUser);

        try (MockedStatic<UserMapper> mocked = Mockito.mockStatic(UserMapper.class)) {
            mocked.when(() -> UserMapper.toEntity(request, passwordEncoder)).thenReturn(appUser);
            mocked.when(() -> UserMapper.toDTO(savedUser)).thenReturn(expectedDto);

            UserDTO result = userService.createUser(request);

            assertEquals(1L, result.getId());
            verify(userRepository).existsByEmail("unittest@mail.com");
            verify(userRepository).save(appUser);
        }
    }


    @Test
    void testCreateUserDuplicateEmailException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        try (MockedStatic<UserMapper> mocked = Mockito.mockStatic(UserMapper.class)) {
            assertThrows(UserService.DuplicateEmailException.class, () -> userService.createUser(request));
            verify(userRepository).existsByEmail("taken@example.com");
            verify(userRepository, never()).save(any());
            verifyNoMoreInteractions(userRepository);
        }
    }



    @Test
    // No stubbing needed; default is do-nothing for void methods unless specified
    void deleteUser() {
        Long id = 1L;

        userService.deleteUser(id);

        verify(userRepository, times(1)).deleteById(id);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void testDeleteUserUserNotFoundException() {
        Long missingId = -1L;
        doThrow(new EmptyResultDataAccessException(1))
                .when(userRepository).deleteById(missingId);

        assertThrows(UserService.UserNotFoundException.class, () -> userService.deleteUser(missingId));
        verify(userRepository, times(1)).deleteById(missingId);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void deleteUserByEmail() {
        String email = "unittest@mail.com";

        userService.deleteUser(email);

        verify(userRepository, times(1)).deleteByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void testDeleteUserByEmailUserNotFoundException() {
        String missingEmail = "notfound@mail.com";

        doThrow(new EmptyResultDataAccessException(1))
                .when(userRepository).deleteByEmail(missingEmail);

        assertThrows(UserService.UserNotFoundException.class, () -> userService.deleteUser(missingEmail));
        verify(userRepository, times(1)).deleteByEmail(missingEmail);
        verifyNoMoreInteractions(userRepository);
    }


    private AppUser createUser(Long userId, String email, String name) {
        var appUser = new AppUser();
        appUser.setId(userId);
        appUser.setName(name);
        appUser.setEmail(email);
        return appUser;
    }
}