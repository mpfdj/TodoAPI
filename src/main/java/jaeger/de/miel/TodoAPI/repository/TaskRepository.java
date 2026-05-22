package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    // Using JPA derived query method name
    List<Task> findTasksByList_IdAndCreator_Id(Long listId, Long creatorId);
//    Optional<Task> findTasksByList_IdAndCreator_IdAndTask_Id(Long listId, Long creatorId, Long taskId);
    Optional<Task> findTaskByIdAndList_IdAndCreator_Id(Long id, Long listId, Long creatorId);
    long deleteByIdAndList_IdAndCreator_Id(Long taskId, Long listId, Long creatorId);

}
