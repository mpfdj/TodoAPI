package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {

    List<Task> findTasksByList_IdAndCreator_Id(Long listId, Long creatorId);  // Using JPA derived query method name
    long deleteByIdAndList_IdAndCreator_Id(Long taskId, Long listId, Long creatorId);  // Using JPA derived query method name

}
