package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.mapper.UserMapper;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getUsers() {
        var userList = new ArrayList<UserDTO>();

        Iterable<AppUser> users = userRepository.findAll();
        users.forEach(u -> userList.add(UserMapper.toDTO(u)));

        userList.sort(Comparator.comparing(UserDTO::getName));
        return userList;
    }

}
