package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    List<Task> findTasksByList_IdAndCreator_Id(Long listId, Long creatorId);  // Using JPA derived query method name
    Optional<Task> findTaskByIdAndList_IdAndCreator_Id(Long id, Long listId, Long creatorId);  // Using JPA derived query method name
    long deleteByIdAndList_IdAndCreator_Id(Long taskId, Long listId, Long creatorId);  // Using JPA derived query method name

}
