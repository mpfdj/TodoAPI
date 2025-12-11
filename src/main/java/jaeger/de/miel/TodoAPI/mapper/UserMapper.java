package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public static UserDTO toDTO(AppUser appUser) {
        var userDTO = new UserDTO();
        userDTO.setId(appUser.getId());
        userDTO.setEmail(appUser.getEmail());
        userDTO.setName(appUser.getName());
        return userDTO;
    }

}
