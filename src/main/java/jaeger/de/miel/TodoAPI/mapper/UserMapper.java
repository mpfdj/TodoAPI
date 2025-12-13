package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserMapper {

    public static UserDTO toDTO(AppUser appUser) {
        var userDTO = new UserDTO();
        userDTO.setId(appUser.getId());
        userDTO.setEmail(appUser.getEmail());
        userDTO.setName(appUser.getName());
        return userDTO;
    }

    public static AppUser toEntity(CreateUserRequestDTO createUserRequestDTO, PasswordEncoder passwordEncoder) {
        String email = createUserRequestDTO.getEmail().trim().toLowerCase();
        String hash = passwordEncoder.encode(createUserRequestDTO.getPassword());
        Instant now = Instant.now();

        var appUser = new AppUser();
        appUser.setName(createUserRequestDTO.getName());
        appUser.setEmail(email);
        appUser.setPasswordHash(hash);
        appUser.setCreatedAt(now);
        appUser.setUpdatedAt(now);
        return appUser;
    }
}
