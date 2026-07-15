package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    // Using JPA derived query method name
    List<Task> findTasksByList_IdAndCreator_Id(Long listId, Long creatorId);
    Optional<Task> findTaskByIdAndList_IdAndCreator_Id(Long id, Long listId, Long creatorId);
    long deleteByIdAndList_IdAndCreator_Id(Long taskId, Long listId, Long creatorId);

    @Query("""
    SELECT COALESCE(MAX(t.sortOrder), 0) + 1
    FROM Task t
    WHERE t.list.id = :listId
    """)
    Integer findNextSortOrder(@Param("listId") Long listId);

}
