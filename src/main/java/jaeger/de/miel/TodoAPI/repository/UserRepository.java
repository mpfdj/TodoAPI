package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<AppUser, Long> {

    boolean existsByEmail(String email);
    void deleteById(Long id);
    void deleteByEmail(String email);

//    @Transactional
//    @Modifying
//    @Query("DELETE FROM AppUser u WHERE u.email = :email")
//    void deleteByEmail(@Param("email") String email);

}
