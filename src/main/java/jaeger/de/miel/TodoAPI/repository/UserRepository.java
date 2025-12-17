package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);
    void deleteByEmail(String email);  // Using JPA derived query method name

}
