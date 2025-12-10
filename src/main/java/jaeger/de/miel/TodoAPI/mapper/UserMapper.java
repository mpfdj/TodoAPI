package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {


    public static UserDTO toDTO(AppUser appUser) {
        var userResponse = new UserDTO();
        userResponse.setId(appUser.getId());
        userResponse.setEmail(appUser.getEmail());
        userResponse.setName(appUser.getName());
        return userResponse;
    }

}
