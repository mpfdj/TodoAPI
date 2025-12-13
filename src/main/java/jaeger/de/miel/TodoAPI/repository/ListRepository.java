package jaeger.de.miel.TodoAPI.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ListRepository extends CrudRepository<jaeger.de.miel.TodoAPI.entity.List,Long> {

    @Query("SELECT l FROM List l WHERE l.owner.id = :ownerId")
    List<jaeger.de.miel.TodoAPI.entity.List> findListsByUserId(@Param("ownerId") Long ownerId);

    boolean existsByOwner_IdAndNameIgnoreCase(Long ownerId, String name);

}
