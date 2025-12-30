package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateUserRequestDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ListMapper {

    public static ListDTO toDTO(List list) {
        var listDTO = new ListDTO();
        listDTO.setId(list.getId());
        listDTO.setUserId(list.getOwner().getId());
        listDTO.setName(list.getName());
        listDTO.setDescription(list.getDescription());
        return listDTO;
    }

    public static List toEntity(Long userId, CreateListRequestDTO createListRequestDTO) {
        AppUser owner = new AppUser();
        owner.setId(userId);
        Instant now = Instant.now();

        var list = new List();
        list.setOwner(owner);
        list.setName(createListRequestDTO.getName());
        list.setDescription(createListRequestDTO.getDescription());
        list.setCreatedAt(now);
        list.setUpdatedAt(now);
        return  list;
    }

    public static List toEntity(List list, UpdateListRequestDTO updateListRequestDTO) {
        String name        = updateListRequestDTO.getName();
        String description = updateListRequestDTO.getDescription();
        Instant now        = Instant.now();

        if (name != null) list.setName(name);
        if (description != null) list.setDescription(description);

        list.setUpdatedAt(now);
        return list;
    }

}
