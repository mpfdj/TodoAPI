package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.jpa.show-sql=true",
//        "logging.level.org.hibernate.SQL=DEBUG",
//        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
//})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    @Transactional
    @Test
    public void testExistsByEmail() {
        var appUser = createUser();
        var created = userRepository.save(appUser);
        System.out.println(created);

        boolean emailExists = userRepository.existsByEmail(appUser.getEmail());
        System.out.println("email exists: " + emailExists);
        assertTrue(emailExists);
    }


    @Transactional
    @Test
    public void testDeleteByEmail() {
        var appUser = createUser();
        userRepository.save(appUser);

        userRepository.deleteByEmail("unittest@mail.com");
        assertFalse(userRepository.existsByEmail("unittest@mail.com"));
    }


    private AppUser createUser() {
        Instant now = Instant.now();

        var appUser = new AppUser();
        appUser.setName("name");
        appUser.setEmail("unittest@mail.com");
        appUser.setPasswordHash("password");
        appUser.setCreatedAt(now);
        appUser.setUpdatedAt(now);
        return appUser;
    }

}
