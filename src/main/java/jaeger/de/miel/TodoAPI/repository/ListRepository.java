package jaeger.de.miel.TodoAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListRepository extends JpaRepository<jaeger.de.miel.TodoAPI.entity.List,Long> {

    List<jaeger.de.miel.TodoAPI.entity.List> findListsByOwner_Id(Long ownerId);  // Using JPA derived query method name
    boolean existsByOwner_IdAndNameIgnoreCase(Long ownerId, String name);
    long deleteByIdAndOwner_Id(Long listId, Long ownerId);

}
