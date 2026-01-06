package jaeger.de.miel.TodoAPI.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JWTTokenGenerator {

    @Value("${security.jwt.secret}")
    private String jwtSecret;  // Same secret as in SecurityConfig

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private SecretKey key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
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
                .expiration(Date.from(Instant.now().plusSeconds(jwtExpiration)))
                .issuer(issuerUri)
                .signWith(key)
                .compact();
    }
}