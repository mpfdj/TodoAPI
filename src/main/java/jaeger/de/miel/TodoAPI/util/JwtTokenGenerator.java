package jaeger.de.miel.TodoAPI.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenGenerator {

    private final SecretKey key;

    public JwtTokenGenerator() {
        // Same secret as in SecurityConfig
        String secret = "mySuperSecretKeyThatIsAtLeast32BytesLong123!";
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateUserToken(String username) {
        return generateToken(username, new String[]{"USER"});
    }

    public String generateAdminToken(String username) {
        return generateToken(username, new String[]{"USER", "ADMIN"});
    }

    private String generateToken(String username, String[] roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("preferred_username", username);
        claims.put("email", username + "@example.com");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))  // 1 hour 3600
                .issuer("http://todo-api")
                .signWith(key)
                .compact();
    }
}