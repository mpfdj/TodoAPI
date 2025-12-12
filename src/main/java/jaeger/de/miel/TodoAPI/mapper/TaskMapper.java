package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.stereotype.Service;

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

}
