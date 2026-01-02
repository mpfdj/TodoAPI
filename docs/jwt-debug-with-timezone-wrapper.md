Here's a complete example using the `JwtTimeZoneWrapper` class:

## **1. First, create the JwtTimeZoneWrapper class:**

```java
package jaeger.de.miel.TodoAPI.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;

/**
 * Wrapper class to display JWT timestamps in different time zones
 */
public class JwtTimeZoneWrapper {
    
    private final Jwt jwt;
    private final ZoneId displayZone;
    
    public JwtTimeZoneWrapper(Jwt jwt) {
        this(jwt, ZoneId.systemDefault());
    }
    
    public JwtTimeZoneWrapper(Jwt jwt, ZoneId displayZone) {
        this.jwt = jwt;
        this.displayZone = displayZone;
    }
    
    // ===== FORMATTING METHODS =====
    
    public String getExpirationInDisplayZone() {
        return formatInstant(jwt.getExpiresAt(), displayZone);
    }
    
    public String getIssuedAtInDisplayZone() {
        return formatInstant(jwt.getIssuedAt(), displayZone);
    }
    
    public String getNotBeforeInDisplayZone() {
        return formatInstant(jwt.getNotBefore(), displayZone);
    }
    
    public String getExpirationInAmsterdam() {
        return formatInstant(jwt.getExpiresAt(), ZoneId.of("Europe/Amsterdam"));
    }
    
    public String getIssuedAtInAmsterdam() {
        return formatInstant(jwt.getIssuedAt(), ZoneId.of("Europe/Amsterdam"));
    }
    
    public String getExpirationInUTC() {
        return formatInstant(jwt.getExpiresAt(), ZoneOffset.UTC);
    }
    
    public String getIssuedAtInUTC() {
        return formatInstant(jwt.getIssuedAt(), ZoneOffset.UTC);
    }
    
    // ===== TIME REMAINING CALCULATIONS =====
    
    public long getSecondsRemaining() {
        return getTimeRemaining(ChronoUnit.SECONDS);
    }
    
    public long getMinutesRemaining() {
        return getTimeRemaining(ChronoUnit.MINUTES);
    }
    
    public long getHoursRemaining() {
        return getTimeRemaining(ChronoUnit.HOURS);
    }
    
    public boolean isExpired() {
        Instant expiresAt = jwt.getExpiresAt();
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    public boolean isAboutToExpire(long minutesThreshold) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return false;
        
        long minutesRemaining = ChronoUnit.MINUTES.between(Instant.now(), expiresAt);
        return minutesRemaining > 0 && minutesRemaining <= minutesThreshold;
    }
    
    // ===== TIMEZONE CONVERSION UTILITIES =====
    
    public LocalDateTime getExpirationAsLocalDateTime(ZoneId zone) {
        Instant exp = jwt.getExpiresAt();
        return exp != null ? LocalDateTime.ofInstant(exp, zone) : null;
    }
    
    public LocalDateTime getIssuedAtAsLocalDateTime(ZoneId zone) {
        Instant iat = jwt.getIssuedAt();
        return iat != null ? LocalDateTime.ofInstant(iat, zone) : null;
    }
    
    // ===== FORMATTED SUMMARIES =====
    
    public Map<String, String> getTimeSummary() {
        return Map.of(
            "exp_utc", getExpirationInUTC(),
            "exp_amsterdam", getExpirationInAmsterdam(),
            "exp_display_zone", getExpirationInDisplayZone(),
            "iat_utc", getIssuedAtInUTC(),
            "iat_amsterdam", getIssuedAtInAmsterdam(),
            "iat_display_zone", getIssuedAtInDisplayZone(),
            "seconds_remaining", String.valueOf(getSecondsRemaining()),
            "is_expired", String.valueOf(isExpired())
        );
    }
    
    // ===== DELEGATE METHODS TO ORIGINAL JWT =====
    
    public String getSubject() {
        return jwt.getSubject();
    }
    
    public String getClaimAsString(String claim) {
        return jwt.getClaimAsString(claim);
    }
    
    public <T> T getClaim(String claim) {
        return jwt.getClaim(claim);
    }
    
    public Map<String, Object> getClaims() {
        return jwt.getClaims();
    }
    
    public String getTokenValue() {
        return jwt.getTokenValue();
    }
    
    public Instant getExpiresAt() {
        return jwt.getExpiresAt();
    }
    
    public Instant getIssuedAt() {
        return jwt.getIssuedAt();
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private String formatInstant(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
               " (" + zoneId.getId() + ")";
    }
    
    private long getTimeRemaining(ChronoUnit unit) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return -1;
        
        Instant now = Instant.now();
        if (now.isAfter(expiresAt)) {
            return -unit.between(expiresAt, now);
        }
        return unit.between(now, expiresAt);
    }
    
    // ===== STATIC UTILITY METHODS =====
    
    public static JwtTimeZoneWrapper of(Jwt jwt) {
        return new JwtTimeZoneWrapper(jwt);
    }
    
    public static JwtTimeZoneWrapper of(Jwt jwt, ZoneId zoneId) {
        return new JwtTimeZoneWrapper(jwt, zoneId);
    }
    
    public static Function<Jwt, JwtTimeZoneWrapper> toAmsterdamWrapper() {
        return jwt -> new JwtTimeZoneWrapper(jwt, ZoneId.of("Europe/Amsterdam"));
    }
    
    public static Function<Jwt, JwtTimeZoneWrapper> toUTCWrapper() {
        return jwt -> new JwtTimeZoneWrapper(jwt, ZoneOffset.UTC);
    }
}
```

## **2. Create a service that uses the wrapper:**

```java
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
```

## **3. Create a controller that uses the wrapper:**

```java
package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.service.TokenInfoService;
import jaeger.de.miel.TodoAPI.util.JwtTimeZoneWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {
    
    private final TokenInfoService tokenInfoService;
    
    public TokenController(TokenInfoService tokenInfoService) {
        this.tokenInfoService = tokenInfoService;
    }
    
    // Example 1: Using wrapper directly in controller method
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(
            @AuthenticationPrincipal Jwt jwt) {
        
        if (jwt == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No JWT token provided"));
        }
        
        // Create wrapper for Amsterdam timezone
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("token_summary", wrapper.getTimeSummary());
        response.put("subject", wrapper.getSubject());
        response.put("claims", wrapper.getClaims());
        
        return ResponseEntity.ok(response);
    }
    
    // Example 2: Get token info for current user
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentTokenInfo() {
        Map<String, Object> info = tokenInfoService.getCurrentTokenInfo();
        return ResponseEntity.ok(info);
    }
    
    // Example 3: Check expiry status
    @GetMapping("/expiry-check")
    public ResponseEntity<Map<String, Object>> checkExpiry(
            @AuthenticationPrincipal Jwt jwt) {
        
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt);
        
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
    
    // Example 4: Compare multiple timezones
    @GetMapping("/timezones")
    public ResponseEntity<Map<String, Object>> getMultipleTimezones(
            @AuthenticationPrincipal Jwt jwt) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Amsterdam
        JwtTimeZoneWrapper amsterdamWrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));
        response.put("amsterdam", Map.of(
            "issued_at", amsterdamWrapper.getIssuedAtInDisplayZone(),
            "expires_at", amsterdamWrapper.getExpirationInDisplayZone()
        ));
        
        // New York
        JwtTimeZoneWrapper nyWrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("America/New_York"));
        response.put("new_york", Map.of(
            "issued_at", nyWrapper.getIssuedAtInDisplayZone(),
            "expires_at", nyWrapper.getExpirationInDisplayZone()
        ));
        
        // Tokyo
        JwtTimeZoneWrapper tokyoWrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Asia/Tokyo"));
        response.put("tokyo", Map.of(
            "issued_at", tokyoWrapper.getIssuedAtInDisplayZone(),
            "expires_at", tokyoWrapper.getExpirationInDisplayZone()
        ));
        
        // UTC
        response.put("utc", Map.of(
            "issued_at", jwt.getIssuedAt(),
            "expires_at", jwt.getExpiresAt()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    // Example 5: Service-based expiry warning
    @GetMapping("/warning")
    public ResponseEntity<Map<String, Object>> getExpiryWarning() {
        Map<String, Object> warning = tokenInfoService.getTokenExpiryWarning();
        return ResponseEntity.ok(warning);
    }
    
    // Example 6: Create a token with specific expiry (for testing)
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeToken(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String targetTimezone = request.getOrDefault("timezone", "Europe/Amsterdam");
        ZoneId zoneId = ZoneId.of(targetTimezone);
        
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt, zoneId);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("timezone", targetTimezone);
        analysis.put("subject", wrapper.getSubject());
        analysis.put("token_valid", !wrapper.isExpired());
        analysis.put("issued_at", wrapper.getIssuedAtInDisplayZone());
        analysis.put("expires_at", wrapper.getExpirationInDisplayZone());
        analysis.put("time_remaining_minutes", wrapper.getMinutesRemaining());
        
        // Calculate when token will expire in target timezone
        analysis.put("expiry_warning", 
            wrapper.isAboutToExpire(10) ? "Will expire soon" : "Valid for a while");
        
        return ResponseEntity.ok(analysis);
    }
}
```

## **4. Create a filter that logs token expiry (optional):**

```java
package jaeger.de.miel.TodoAPI.filter;

import jaeger.de.miel.TodoAPI.util.JwtTimeZoneWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;

@Component
public class TokenExpiryLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenExpiryLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));
                
                // Log if token is about to expire
                if (wrapper.isAboutToExpire(5)) {
                    logger.warn("Token for user {} will expire in {} minutes at {} (Amsterdam time)", 
                            wrapper.getSubject(), 
                            wrapper.getMinutesRemaining(),
                            wrapper.getExpirationInDisplayZone());
                }
                
                // Add expiry info to response headers
                if (!wrapper.isExpired()) {
                    response.addHeader("X-Token-Expires-In-Minutes", 
                            String.valueOf(wrapper.getMinutesRemaining()));
                    response.addHeader("X-Token-Expires-At-Amsterdam", 
                            wrapper.getExpirationInAmsterdam());
                }
            }
        } catch (Exception e) {
            // Don't break the filter chain on logging errors
            logger.debug("Error logging token expiry: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## **5. Example usage in tests:**

```java
package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.util.JwtTimeZoneWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTimeZoneWrapperTest {
    
    @Test
    void testJwtTimeZoneWrapper() {
        // Create a test JWT
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("testuser")
                .claim("roles", new String[]{"USER"})
                .issuedAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build();
        
        // Create wrapper
        JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt);
        
        // Test methods
        assertThat(wrapper.getSubject()).isEqualTo("testuser");
        assertThat(wrapper.isExpired()).isFalse();
        assertThat(wrapper.getMinutesRemaining()).isGreaterThan(0);
        
        // Get time summary
        Map<String, String> summary = wrapper.getTimeSummary();
        assertThat(summary).containsKey("exp_utc");
        assertThat(summary).containsKey("exp_amsterdam");
        assertThat(summary).containsKey("is_expired");
        
        System.out.println("UTC Expiry: " + wrapper.getExpirationInUTC());
        System.out.println("Amsterdam Expiry: " + wrapper.getExpirationInAmsterdam());
        System.out.println("Minutes remaining: " + wrapper.getMinutesRemaining());
    }
    
    @Test
    void testStaticFactoryMethods() {
        Jwt jwt = Jwt.withTokenValue("test")
                .subject("user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusHours(1))
                .build();
        
        // Using static factory methods
        JwtTimeZoneWrapper amsterdamWrapper = JwtTimeZoneWrapper.toAmsterdamWrapper().apply(jwt);
        JwtTimeZoneWrapper utcWrapper = JwtTimeZoneWrapper.toUTCWrapper().apply(jwt);
        
        assertThat(amsterdamWrapper.getExpirationInDisplayZone()).contains("Europe/Amsterdam");
        assertThat(utcWrapper.getExpirationInDisplayZone()).contains("Z");
    }
}
```

## **How to use it in practice:**

1. **Inject in your services/controllers:**
```java
@GetMapping("/secure-data")
public ResponseEntity<?> getSecureData(@AuthenticationPrincipal Jwt jwt) {
    JwtTimeZoneWrapper wrapper = JwtTimeZoneWrapper.of(jwt, ZoneId.of("Europe/Amsterdam"));
    
    // Check if token is about to expire
    if (wrapper.isAboutToExpire(2)) {
        // Return warning or trigger token refresh
        return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                .header("X-Token-Expired-Soon", "true")
                .body("Please refresh your token");
    }
    
    // Continue with normal processing...
}
```

2. **In logs or monitoring:**
```java
logger.info("Token for {} expires at {} (Amsterdam time)", 
    wrapper.getSubject(), 
    wrapper.getExpirationInAmsterdam());
```

3. **For UI display:**
```java
// In your REST response
{
  "user": "john",
  "token_expires_at": "2024-01-15 15:30:00 (Europe/Amsterdam)",
  "time_remaining_minutes": 45
}
```

The wrapper provides a clean, reusable way to handle JWT timezone conversions throughout your application while keeping the UTC-based JWT standard intact.