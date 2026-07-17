package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.CreateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.dto.TaskSortOrderRequestDTO;
import jaeger.de.miel.TodoAPI.dto.UpdateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.Task;
import jaeger.de.miel.TodoAPI.mapper.TaskMapper;
import jaeger.de.miel.TodoAPI.repository.ListRepository;
import jaeger.de.miel.TodoAPI.repository.TaskRepository;
import jaeger.de.miel.TodoAPI.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@AllArgsConstructor
@Service
public class TaskService {

    private UserRepository userRepository;
    private ListRepository listRepository;
    private TaskRepository taskRepository;

    public List<TaskDTO> getTasks(Long userId, Long listId) {

        List<Task> tasks = taskRepository.findTasksByList_IdAndCreator_Id(listId, userId);

        List<TaskDTO> taskList = new ArrayList<>();
        tasks.forEach(t -> taskList.add(TaskMapper.toDTO(t)));
        return taskList;
    }

    public TaskDTO getTask(Long userId, Long listId, Long taskId) {
        Task task = taskRepository.findTaskByIdAndList_IdAndCreator_Id(taskId, listId, userId)
                .orElseThrow(() -> new TaskNotFoundException("TaskId not found: " + taskId));
        return TaskMapper.toDTO(task);

    }

    public TaskDTO createTask(Long userId, Long listId, CreateTaskRequestDTO createTaskRequestDTO) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CreatorNotFoundException("CreatorId not found: " + userId));

        listRepository.findById(listId)
                .orElseThrow(() -> new ListNotFoundException("ListId not found: " + listId));

        Integer nextSortOrder = taskRepository.findNextSortOrder(listId);
        Task task = taskRepository.save(TaskMapper.toEntity(userId, listId, nextSortOrder, createTaskRequestDTO));
        return TaskMapper.toDTO(task);
    }


    public void deleteTask(Long userId, Long listId, Long taskId) {
        try {
            taskRepository.deleteByIdAndList_IdAndCreator_Id(taskId, listId, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }
    }


    public TaskDTO updateTask(Long userId, Long listId, Long taskId, UpdateTaskRequestDTO request) {

        Task task = taskRepository.findTaskByIdAndList_IdAndCreator_Id(taskId, listId, userId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with userId: " + userId + " and listId: " + listId + " and taskId: " + taskId));

        Task entity = TaskMapper.toEntity(task, request);
        Task updated = taskRepository.save(entity);

        return TaskMapper.toDTO(updated);
    }


    public void updateTaskSortOrder(List<TaskSortOrderRequestDTO> sortOrders) {
        sortOrders.forEach(sortOrderRequestDTO -> {
            Task task = taskRepository.findById(sortOrderRequestDTO.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found with ID: " + sortOrderRequestDTO.getTaskId()));
            task.setSortOrder(sortOrderRequestDTO.getSortOrder());
            taskRepository.save(task);
        });
    }


    public List<Task> getTasksByListId(Long listId) {
        return taskRepository.findByListIdOrderBySortOrderAsc(listId);
    }


    // ---------------------------------------
    // Exceptions
    // ---------------------------------------
    public static class CreatorNotFoundException extends RuntimeException {
        public CreatorNotFoundException(String message) {
            super(message);
        }
    }

    public static class ListNotFoundException extends RuntimeException {
        public ListNotFoundException(String message) {
            super(message);
        }
    }

    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String message) {
            super(message);
        }
    }

}
