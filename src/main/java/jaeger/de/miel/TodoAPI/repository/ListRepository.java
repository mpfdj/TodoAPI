package jaeger.de.miel.TodoAPI.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ListRepository extends CrudRepository<jaeger.de.miel.TodoAPI.entity.List,Long> {

//    @Query("SELECT l FROM List l WHERE l.owner.id = :ownerId")
//    List<jaeger.de.miel.TodoAPI.entity.List> findListsByUserId(@Param("ownerId") Long ownerId);

    List<jaeger.de.miel.TodoAPI.entity.List> findListsByOwner_Id(Long ownerId);
    boolean existsByOwner_IdAndNameIgnoreCase(Long ownerId, String name);

}
