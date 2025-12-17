package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {

//    @Query("SELECT t FROM Task t WHERE t.creator.id = :userId AND t.list.id = :listId")
//    List<Task> findTasksByUserIdAndListId(
//            @Param("userId") Long userId,
//            @Param("listId") Long listId);


    List<Task> findTasksByList_IdAndCreator_Id(Long listId, Long creatorId);  // Using JPA derived query method name
    long deleteByIdAndList_IdAndCreator_Id(Long taskId, Long listId, Long creatorId);  // Using JPA derived query method name
    void deleteById(Long taskId);

}
