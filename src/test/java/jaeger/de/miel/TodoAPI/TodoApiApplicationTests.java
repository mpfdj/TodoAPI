package jaeger.de.miel.TodoAPI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class TodoApiApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void generateHash() {
        String password = "user123";
        String hashed = passwordEncoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hashed);
    }

}
