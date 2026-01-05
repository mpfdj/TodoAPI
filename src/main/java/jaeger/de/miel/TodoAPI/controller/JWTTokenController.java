package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.util.JWTTokenGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/tokens")
public class JWTTokenController {

    private final JWTTokenGenerator tokenGenerator;

    public JWTTokenController(JWTTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @GetMapping("/user")
    public Map<String, String> getUserToken() {
        String token = tokenGenerator.generateUserToken("testuser");
        return Map.of("token", token, "message", "Use for USER endpoints");
    }

    @GetMapping("/admin")
    public Map<String, String> getAdminToken() {
        String token = tokenGenerator.generateAdminToken("testadmin");
        return Map.of("token", token, "message", "Use for ADMIN endpoints");
    }
}
