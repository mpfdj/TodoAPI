package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);
    void deleteById(Long id);
    void deleteByEmail(String email);  // Using JPA derived query method name


//    @Transactional
//    @Modifying
//    @Query("DELETE FROM AppUser u WHERE u.email = :email")
//    void deleteByEmail(@Param("email") String email);

}
