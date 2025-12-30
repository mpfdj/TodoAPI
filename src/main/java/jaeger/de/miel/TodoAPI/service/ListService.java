package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.*;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.mapper.ListMapper;
import jaeger.de.miel.TodoAPI.mapper.UserMapper;
import jaeger.de.miel.TodoAPI.repository.ListRepository;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Transactional
@AllArgsConstructor
@Service
public class ListService {

    private final ListRepository listRepository;
    private final UserRepository userRepository;


    public List<ListDTO> getLists(Long userId) {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByOwner_Id(userId);

        List<ListDTO> listList = new ArrayList<>();
        lists.forEach(l -> listList.add(ListMapper.toDTO(l)));
        listList.sort(Comparator.comparing(ListDTO::getName));

        return listList;
    }


    public ListDTO createList(Long userId, CreateListRequestDTO request) {
        String name = request.getName();

        AppUser owner = userRepository.findById(userId)
                .orElseThrow(() -> new OwnerNotFoundException("OwnerId not found: " + userId));

        if (listRepository.existsByOwner_IdAndNameIgnoreCase(userId, name)) {
            throw new DuplicateListNameException("List name already exists for owner: '" + name + "'");
        }

        jaeger.de.miel.TodoAPI.entity.List list = listRepository.save(ListMapper.toEntity(userId, request));
        return ListMapper.toDTO(list);
    }


    public void deleteList(Long userId, Long listId) {
        try {
            listRepository.deleteByIdAndOwner_Id(listId, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ListNotFoundException("List not found with id: " + listId);
        }
    }


    public ListDTO updateList(Long userId, Long listId, UpdateListRequestDTO request) {
        jaeger.de.miel.TodoAPI.entity.List list = listRepository.findListByIdAndOwner_Id(listId, userId)
                .orElseThrow(() -> new ListNotFoundException("List not found with userId: " + userId + " and listId: " + listId));

        jaeger.de.miel.TodoAPI.entity.List entity = ListMapper.toEntity(list, request);
        jaeger.de.miel.TodoAPI.entity.List updated = listRepository.save(entity);

        return ListMapper.toDTO(updated);
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    public static class OwnerNotFoundException extends RuntimeException {
        public OwnerNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateListNameException extends RuntimeException {
        public DuplicateListNameException(String message) {
            super(message);
        }
    }

    public static class ListNotFoundException extends RuntimeException {
        public ListNotFoundException(String message) {
            super(message);
        }
    }

}