package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.dto.UserDTO;
import jaeger.de.miel.TodoAPI.mapper.ListMapper;
import jaeger.de.miel.TodoAPI.repository.ListRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class ListService {

    private final ListRepository listRepository;

    public List<ListDTO> getLists(Long userId) {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByUserId(userId);

        List<ListDTO> listList = new ArrayList<>();
        lists.forEach(l -> listList.add(ListMapper.toDTO(l)));
        listList.sort(Comparator.comparing(ListDTO::getName));

        return listList;
    }

}
