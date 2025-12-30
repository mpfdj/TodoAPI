package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.*;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
import jaeger.de.miel.TodoAPI.entity.Task;
import jakarta.validation.constraints.FutureOrPresent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class TaskMapper {

    public static TaskDTO toDTO(Task task) {
        var taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setListId(task.getList().getId());
        taskDTO.setUserId(task.getCreator().getId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setDueDate(task.getDueDate());
        taskDTO.setPriority(task.getPriority());
        taskDTO.setCreatedAt(task.getCreatedAt());
        taskDTO.setUpdatedAt(task.getUpdatedAt());
        taskDTO.setCompletedAt(task.getCompletedAt());
        return taskDTO;
    }

    public static Task toEntity(Long userId, Long listId, CreateTaskRequestDTO createTaskRequestDTO) {
        List list = new List();
        list.setId(listId);

        AppUser creator = new AppUser();
        creator.setId(userId);

        Instant now = Instant.now();

        var task = new Task();
        task.setList(list);
        task.setCreator(creator);
        task.setTitle(createTaskRequestDTO.getTitle());
        task.setDescription(createTaskRequestDTO.getDescription());
        task.setStatus(createTaskRequestDTO.getStatus().toString());
        task.setDueDate(createTaskRequestDTO.getDueDate());
        task.setPriority(createTaskRequestDTO.getPriority());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return task;
    }

    public static Task toEntity(Task task, UpdateTaskRequestDTO updateTaskRequestDTO) {
        String title       = updateTaskRequestDTO.getTitle();
        String description = updateTaskRequestDTO.getDescription();
        TaskStatus status  = updateTaskRequestDTO.getStatus();
        LocalDate dueDate  = updateTaskRequestDTO.getDueDate();
        Integer priority   = updateTaskRequestDTO.getPriority();
        Instant now        = Instant.now();

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(status.toString());
        if (dueDate != null) task.setDueDate(dueDate);
        if (priority != null) task.setPriority(priority);
        task.setUpdatedAt(now);

        return task;
    }

}
