package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.entity.Task;
import jaeger.de.miel.TodoAPI.mapper.TaskMapper;
import jaeger.de.miel.TodoAPI.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class TaskService {

    private TaskRepository taskRepository;

    public List<TaskDTO> getTasks(Long userId, Long listId) {

        List<Task> tasks = taskRepository.findTasksByUserIdAndListId(userId, listId);

        List<TaskDTO> taskList = new ArrayList<>();
        tasks.forEach(t -> taskList.add(TaskMapper.toDTO(t)));
        return taskList;
    }

}
