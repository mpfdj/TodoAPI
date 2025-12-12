package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task,Long> {

    @Query("SELECT t FROM Task t WHERE t.creator.id = :userId AND t.list.id = :listId")
    List<Task> findTasksByUserIdAndListId(
            @Param("userId") Long userId,
            @Param("listId") Long listId);

}
