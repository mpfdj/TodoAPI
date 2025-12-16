package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.stereotype.Service;

import java.time.Instant;

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


}
