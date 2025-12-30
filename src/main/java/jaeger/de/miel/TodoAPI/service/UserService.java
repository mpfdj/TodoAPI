package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateUserRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.mapper.UserMapper;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Transactional
@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public List<UserDTO> getUsers() {
        List<AppUser> users = userRepository.findAll();

        ArrayList<UserDTO> userList = new ArrayList<>();
        users.forEach(u -> userList.add(UserMapper.toDTO(u)));
        userList.sort(Comparator.comparing(UserDTO::getName));

        return userList;
    }


    public UserDTO createUser(CreateUserRequestDTO request) {
        AppUser appUser = UserMapper.toEntity(request, passwordEncoder);
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already in use: " + email);
        }

        AppUser appUserCreated = userRepository.save(appUser);
        return UserMapper.toDTO(appUserCreated);
    }


    public void deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }


    public void deleteUser(String email) {
        try {
            email = email.trim().toLowerCase();
            userRepository.deleteByEmail(email);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }


    public UserDTO updateUser(Long id, UpdateUserRequestDTO request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        String email = request.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already in use: " + email);
        }

        AppUser appUser = UserMapper.toEntity(user, request, passwordEncoder);

        AppUser updated = userRepository.save(appUser);
        return UserMapper.toDTO(updated);
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

}
