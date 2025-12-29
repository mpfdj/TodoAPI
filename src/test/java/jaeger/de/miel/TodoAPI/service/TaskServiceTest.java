package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.Task;
import jaeger.de.miel.TodoAPI.mapper.TaskMapper;
import jaeger.de.miel.TodoAPI.repository.ListRepository;
import jaeger.de.miel.TodoAPI.repository.TaskRepository;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListRepository listRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final Long userId = 1L;
    private final Long listId = 10L;
    private final Long taskId = 100L;

    // ---------------------------------------------------------
    // getTasks
    // ---------------------------------------------------------
    @Test
    void getTasks() {
        Task task = new Task();
        TaskDTO dto = new TaskDTO();

        when(taskRepository.findTasksByList_IdAndCreator_Id(listId, userId))
                .thenReturn(List.of(task));

        try (MockedStatic<TaskMapper> mapper = Mockito.mockStatic(TaskMapper.class)) {
            mapper.when(() -> TaskMapper.toDTO(task)).thenReturn(dto);

            List<TaskDTO> result = taskService.getTasks(userId, listId);

            assertEquals(1, result.size());
            assertEquals(dto, result.get(0));
        }
    }

    // ---------------------------------------------------------
    // createTask
    // ---------------------------------------------------------
    @Test
    void createTask() {
        AppUser user = new AppUser();
        jaeger.de.miel.TodoAPI.entity.List list = new jaeger.de.miel.TodoAPI.entity.List();
        CreateTaskRequestDTO request = new CreateTaskRequestDTO();

        Task taskEntity = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(listRepository.findById(listId)).thenReturn(Optional.of(list));

        try (MockedStatic<TaskMapper> mapper = Mockito.mockStatic(TaskMapper.class)) {
            mapper.when(() -> TaskMapper.toEntity(userId, listId, request))
                    .thenReturn(taskEntity);

            when(taskRepository.save(taskEntity)).thenReturn(taskEntity);

            mapper.when(() -> TaskMapper.toDTO(taskEntity)).thenReturn(taskDTO);

            TaskDTO result = taskService.createTask(userId, listId, request);

            assertEquals(taskDTO, result);
        }
    }

    @Test
    void createTask_throwsCreatorNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(TaskService.CreatorNotFoundException.class,
                () -> taskService.createTask(userId, listId, new CreateTaskRequestDTO()));
    }

    @Test
    void createTask_throwsListNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new AppUser()));
        when(listRepository.findById(listId)).thenReturn(Optional.empty());

        assertThrows(TaskService.ListNotFoundException.class,
                () -> taskService.createTask(userId, listId, new CreateTaskRequestDTO()));
    }

    // ---------------------------------------------------------
    // deleteTask
    // ---------------------------------------------------------
    @Test
    void deleteTask() {
        taskService.deleteTask(userId, listId, taskId);

        verify(taskRepository)
                .deleteByIdAndList_IdAndCreator_Id(taskId, listId, userId);
    }

    @Test
    void deleteTask_throwsTaskNotFound() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(taskRepository)
                .deleteByIdAndList_IdAndCreator_Id(taskId, listId, userId);

        assertThrows(TaskService.TaskNotFoundException.class,
                () -> taskService.deleteTask(userId, listId, taskId));
    }
}
