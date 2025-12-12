package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<AppUser, Long> {

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM AppUser u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

}
