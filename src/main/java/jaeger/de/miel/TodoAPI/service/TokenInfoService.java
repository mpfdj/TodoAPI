package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.util.JwtTimeZoneWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenInfoService {

    public Map<String, Object> getCurrentTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return Map.of("error", "No JWT token found in security context");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));

        Map<String, Object> info = new HashMap<>();

        // Basic token info
        info.put("subject", wrapper.getSubject());
        info.put("token_id", jwt.getId());
        info.put("issuer", jwt.getIssuer());
        info.put("audience", jwt.getAudience());

        // Time information in different zones
        info.put("issued_at_utc", wrapper.getIssuedAtInUTC());
        info.put("issued_at_amsterdam", wrapper.getIssuedAtInAmsterdam());
        info.put("expires_at_utc", wrapper.getExpirationInUTC());
        info.put("expires_at_amsterdam", wrapper.getExpirationInAmsterdam());

        // Status and remaining time
        info.put("is_expired", wrapper.isExpired());
        info.put("is_about_to_expire", wrapper.isAboutToExpire(5)); // 5 minutes threshold
        info.put("seconds_remaining", wrapper.getSecondsRemaining());
        info.put("minutes_remaining", wrapper.getMinutesRemaining());
        info.put("hours_remaining", wrapper.getHoursRemaining());

        // Claims
        info.put("roles", jwt.getClaimAsStringList("roles"));
        info.put("email", jwt.getClaimAsString("email"));
        info.put("preferred_username", jwt.getClaimAsString("preferred_username"));

        return info;
    }

    public Map<String, Object> getTokenExpiryWarning() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return Map.of("warning", "No active session");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt);

        Map<String, Object> warning = new HashMap<>();

        if (wrapper.isExpired()) {
            warning.put("level", "ERROR");
            warning.put("message", "Token has expired!");
            warning.put("expired_at", wrapper.getExpirationInDisplayZone());
        } else if (wrapper.isAboutToExpire(2)) { // 2 minutes
            warning.put("level", "WARNING");
            warning.put("message", "Token will expire in less than 2 minutes");
            warning.put("expires_at", wrapper.getExpirationInDisplayZone());
            warning.put("minutes_remaining", wrapper.getMinutesRemaining());
        } else if (wrapper.isAboutToExpire(5)) { // 5 minutes
            warning.put("level", "INFO");
            warning.put("message", "Token will expire soon");
            warning.put("expires_at", wrapper.getExpirationInDisplayZone());
            warning.put("minutes_remaining", wrapper.getMinutesRemaining());
        } else {
            warning.put("level", "OK");
            warning.put("message", "Token is valid");
            warning.put("expires_at", wrapper.getExpirationInDisplayZone());
            warning.put("hours_remaining", wrapper.getHoursRemaining());
        }

        return warning;
    }
}