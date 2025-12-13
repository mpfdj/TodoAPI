package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
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

    public static List toEntity(CreateListRequestDTO createListRequestDTO) {
        AppUser owner = new AppUser();
        owner.setId(createListRequestDTO.getUserId());
        Instant now = Instant.now();

        var list = new List();
        list.setOwner(owner);
        list.setName(createListRequestDTO.getName());
        list.setDescription(createListRequestDTO.getDescription());
        list.setCreatedAt(now);
        list.setUpdatedAt(now);
        return  list;
    }
}
