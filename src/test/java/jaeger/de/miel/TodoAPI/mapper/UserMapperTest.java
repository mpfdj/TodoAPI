package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
import org.h2.engine.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AppUser appUserMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CreateUserRequestDTO createUserRequestDTOMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    PasswordEncoder passwordEncoderMock;


    @Test
    public void testToDTO() {
        when(appUserMock.getId()).thenReturn(1L);
        when(appUserMock.getEmail()).thenReturn("unittest@mail.com");
        when(appUserMock.getName()).thenReturn("name");

        UserDTO userDTO = UserMapper.toDTO(appUserMock);

        assertEquals(1L, userDTO.getId());
        assertEquals("unittest@mail.com", userDTO.getEmail());
        assertEquals("name", userDTO.getName());
    }


    @Test
    public void testToEntity() {
        when(createUserRequestDTOMock.getName()).thenReturn("name");
        when(createUserRequestDTOMock.getEmail()).thenReturn("unittest@mail.com");
        when(createUserRequestDTOMock.getPassword()).thenReturn("password");
        when(passwordEncoderMock.encode("password")).thenReturn("password hash");

        Instant before = Instant.now();

        AppUser user = UserMapper.toEntity(createUserRequestDTOMock, passwordEncoderMock);
        System.out.println(user);

        Instant after = Instant.now();

        // Assertions
        assertEquals("unittest@mail.com", user.getEmail());
        assertEquals("name", user.getName());
        assertEquals("password hash", user.getPasswordHash());

        // Timestamps assertions
        assertThat(user.getCreatedAt()).isBetween(before, after);
        assertThat(user.getUpdatedAt()).isEqualTo(user.getCreatedAt());
    }

}
