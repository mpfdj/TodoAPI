package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.mapper.ListMapper;
import jaeger.de.miel.TodoAPI.repository.ListRepository;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListServiceTest {

    @InjectMocks
    private ListService listService;

    @Mock
    private ListRepository listRepository;

    @Mock
    private UserRepository userRepository;


    @Test
    public void testGetLists() {
        Long userId = 1L;

        var l1 = createList("Zeta");
        var l2 = createList("Alpha");
        var l3 = createList("Beta");
        List<jaeger.de.miel.TodoAPI.entity.List> lists = Arrays.asList(l1, l2, l3);

        when(listRepository.findListsByOwner_Id(userId)).thenReturn(lists);

        try (MockedStatic<ListMapper> mocked = Mockito.mockStatic(ListMapper.class)) {
            mocked.when(() -> ListMapper.toDTO(l1)).thenAnswer(inv -> {
                ListDTO dto = new ListDTO();
                dto.setName(l1.getName());
                return dto;
            });
            mocked.when(() -> ListMapper.toDTO(l2)).thenAnswer(inv -> {
                ListDTO dto = new ListDTO();
                dto.setName(l2.getName());
                return dto;
            });
            mocked.when(() -> ListMapper.toDTO(l3)).thenAnswer(inv -> {
                ListDTO dto = new ListDTO();
                dto.setName(l3.getName());
                return dto;
            });

            List<ListDTO> result = listService.getLists(userId);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());

            // Ensure sorted ascending by name: Alpha, Beta, Zeta
            assertEquals("Alpha", result.get(0).getName());
            assertEquals("Beta", result.get(1).getName());
            assertEquals("Zeta", result.get(2).getName());

        }
    }


    @Test
    void testGetListsEmpty() {
        Long userId = -1L;
        when(listRepository.findListsByOwner_Id(userId)).thenReturn(List.of());

        try (MockedStatic<ListMapper> mocked = Mockito.mockStatic(ListMapper.class)) {
            List<ListDTO> result = listService.getLists(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(listRepository).findListsByOwner_Id(userId);
            mocked.verifyNoInteractions();   // No mapping interactions when no entities
        }

        verifyNoMoreInteractions(listRepository);
    }


    @Test
    void createList() {
        // Arrange
        Long userId = 10L;
        CreateListRequestDTO request = new CreateListRequestDTO();
        request.setName("Work");

        AppUser owner = new AppUser();
        owner.setId(userId);

        // The entity created by the mapper
        jaeger.de.miel.TodoAPI.entity.List toSave = new jaeger.de.miel.TodoAPI.entity.List();
        toSave.setName("Work");
        toSave.setOwner(owner);

        // The entity returned by repository after persist (e.g., with id set)
        jaeger.de.miel.TodoAPI.entity.List persisted = new jaeger.de.miel.TodoAPI.entity.List();
        persisted.setId(123L);
        persisted.setName("Work");
        persisted.setOwner(owner);

        ListDTO expectedDto = new ListDTO();
        expectedDto.setId(123L);
        expectedDto.setName("Work");

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(listRepository.existsByOwner_IdAndNameIgnoreCase(userId, "Work")).thenReturn(false);
        when(listRepository.save(any(jaeger.de.miel.TodoAPI.entity.List.class))).thenReturn(persisted);

        try (MockedStatic<ListMapper> mocked = Mockito.mockStatic(ListMapper.class)) {
            mocked.when(() -> ListMapper.toEntity(userId, request)).thenReturn(toSave);
            mocked.when(() -> ListMapper.toDTO(persisted)).thenReturn(expectedDto);

            // Act
            ListDTO result = listService.createList(userId, request);

            // Assert
            assertNotNull(result);
            assertEquals(123L, result.getId());
            assertEquals("Work", result.getName());

            // Interactions
            verify(userRepository).findById(userId);
            verify(listRepository).existsByOwner_IdAndNameIgnoreCase(userId, "Work");
            verify(listRepository).save(toSave);

            mocked.verify(() -> ListMapper.toEntity(userId, request));
            mocked.verify(() -> ListMapper.toDTO(persisted));
        }

        verifyNoMoreInteractions(listRepository, userRepository);
    }




    @Test
    void createListOwnerNotFoundException() {
        Long userId = 99L;

        CreateListRequestDTO request = new CreateListRequestDTO();
        request.setName("Home");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        try (MockedStatic<ListMapper> mocked = Mockito.mockStatic(ListMapper.class)) {
            assertThrows(ListService.OwnerNotFoundException.class,
                    () -> listService.createList(userId, request));

                        verify(userRepository).findById(userId);  // Only findById should be called; no further interactions
            verify(listRepository, never()).existsByOwner_IdAndNameIgnoreCase(anyLong(), anyString());
            verify(listRepository, never()).save(any());

            mocked.verifyNoInteractions();
        }

        verifyNoMoreInteractions(userRepository, listRepository);
    }



    @Test
    void createListDuplicateListNameException() {
        Long userId = 10L;

        CreateListRequestDTO request = new CreateListRequestDTO();
        request.setName("Work");

        AppUser owner = new AppUser();
        owner.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(listRepository.existsByOwner_IdAndNameIgnoreCase(userId, "Work")).thenReturn(true);

        try (MockedStatic<ListMapper> mocked = Mockito.mockStatic(ListMapper.class)) {
            assertThrows(ListService.DuplicateListNameException.class,
                    () -> listService.createList(userId, request));

            verify(userRepository).findById(userId);
            verify(listRepository).existsByOwner_IdAndNameIgnoreCase(userId, "Work");
            verify(listRepository, never()).save(any());

            mocked.verifyNoInteractions();  // Mapper should not be called when duplicate detected
        }

        verifyNoMoreInteractions(userRepository, listRepository);
    }


    @Test
    void deleteList() {
        Long userId = 1L;
        Long listId = 1L;

        listService.deleteList(userId, listId);

        verify(listRepository, times(1)).deleteByIdAndOwner_Id(listId, userId);
        verifyNoMoreInteractions(listRepository);
    }


    @Test
    void deleteListListNotFoundException() {
        Long userId = 1L;
        Long missingListId = -1L;

        doThrow(new EmptyResultDataAccessException(1))
                .when(listRepository).deleteByIdAndOwner_Id(missingListId, userId);

        assertThrows(ListService.ListNotFoundException.class,
                () -> listService.deleteList(userId, missingListId));

        verify(listRepository, times(1)).deleteByIdAndOwner_Id(missingListId, userId);
        verify(listRepository, times(1)).deleteByIdAndOwner_Id(missingListId, userId);
        verifyNoMoreInteractions(listRepository);
    }


    private jaeger.de.miel.TodoAPI.entity.List createList(String name) {
        var list = new jaeger.de.miel.TodoAPI.entity.List();
        list.setName(name);
        return list;
    }

}