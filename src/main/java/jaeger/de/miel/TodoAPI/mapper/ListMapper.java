package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.entity.List;
import org.springframework.stereotype.Service;

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

}
