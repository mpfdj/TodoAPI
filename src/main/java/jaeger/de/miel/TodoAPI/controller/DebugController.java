package jaeger.de.miel.TodoAPI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String secret = request.get("secret") != null ?
                request.get("secret") : "mySuperSecretKeyThatIsAtLeast32BytesLong123!";

        Map<String, Object> response = new HashMap<>();

        try {
            // Try to decode the token
            byte[] keyBytes = Base64.getDecoder().decode(
                    Base64.getEncoder().encodeToString(secret.getBytes())
            );
            SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");

            JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
            Jwt jwt = decoder.decode(token);

            response.put("status", "VALID");
            response.put("claims", jwt.getClaims());
            response.put("subject", jwt.getSubject());
            response.put("roles", jwt.getClaimAsStringList("roles"));
            response.put("issuer", jwt.getIssuer());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "INVALID");
            response.put("error", e.getMessage());
            response.put("secretUsed", secret);
            response.put("secretLength", secret.length());

            return ResponseEntity.badRequest().body(response);
        }
    }
}