package jaeger.de.miel.TodoAPI.controller;


import jaeger.de.miel.TodoAPI.service.JWTTokenInfoService;
import jaeger.de.miel.TodoAPI.util.JWTTimeZoneWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class JWTDebugTimeZoneWrapperController {

    public JWTDebugTimeZoneWrapperController(JWTTokenInfoService JWTTokenInfoService) {
    }

    // Using wrapper directly in controller method
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No JWT token provided"));
        }

        // Create wrapper for Amsterdam timezone
        JWTTimeZoneWrapper wrapper = JWTTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));

        Map<String, Object> response = new HashMap<>();
        response.put("token_summary", wrapper.getTimeSummary());
        response.put("subject", wrapper.getSubject());
        response.put("claims", wrapper.getClaims());

        return ResponseEntity.ok(response);
    }

    // Check expiry status
    @GetMapping("/expiry-check")
    public ResponseEntity<Map<String, Object>> checkExpiry(
            @AuthenticationPrincipal Jwt jwt) {

        JWTTimeZoneWrapper wrapper = JWTTimeZoneWrapper.of(jwt);

        Map<String, Object> response = new HashMap<>();
        response.put("status", wrapper.isExpired() ? "EXPIRED" : "VALID");
        response.put("expired", wrapper.isExpired());
        response.put("expires_at_utc", wrapper.getExpirationInUTC());
        response.put("expires_at_amsterdam", wrapper.getExpirationInAmsterdam());
        response.put("expires_at_local", wrapper.getExpirationInDisplayZone());
        response.put("seconds_remaining", wrapper.getSecondsRemaining());

        if (wrapper.isAboutToExpire(5)) {
            response.put("warning", "Token will expire in " + wrapper.getMinutesRemaining() + " minutes");
        }

        return ResponseEntity.ok(response);
    }

}