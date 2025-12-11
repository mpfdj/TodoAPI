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
        Iterable<AppUser> users = userRepository.findAll();

        var userList = new ArrayList<UserDTO>();
        users.forEach(u -> userList.add(UserMapper.toDTO(u)));
        userList.sort(Comparator.comparing(UserDTO::getName));

        return userList;
    }

}
