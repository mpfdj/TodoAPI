The time difference is likely due to **timezone or system clock issues**. The timestamps in your JWT show `"iat": "2025-12-30T23:59:56Z"` (issued) and `"exp": "2025-12-31T00:59:56Z"` (expires), which is exactly 1 hour difference. The `Z` indicates UTC time.

Here are several solutions to fix the time issue:

## Solution 1: Force UTC Time in Token Generation

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtService {
    
    public String generateToken(String username, String[] roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("preferred_username", username);
        claims.put("email", username + "@example.com");
        
        String secret = "mySuperSecretKeyThatIsAtLeast32BytesLong123!";
        var key = Keys.hmacShaKeyFor(secret.getBytes());
        
        // Force UTC time
        Instant now = Instant.now(); // This should already be UTC
        
        System.out.println("Current UTC time: " + now);
        System.out.println("Current system time: " + new Date());
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .issuer("http://todo-api")
                .signWith(key)
                .compact();
    }
}
```

## Solution 2: Add Time Debugging Endpoint

Create a debug endpoint to check server time:

```java
package jaeger.de.miel.TodoAPI.controller;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class TimeDebugController {
    
    @GetMapping("/time")
    public Map<String, String> getServerTime() {
        Map<String, String> times = new HashMap<>();
        
        Instant instant = Instant.now();
        Date date = new Date();
        
        times.put("Instant.now()", instant.toString());
        times.put("new Date()", date.toString());
        times.put("System.currentTimeMillis()", String.valueOf(System.currentTimeMillis()));
        times.put("LocalDateTime.now(UTC)", LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        times.put("LocalDateTime.now(SystemDefault)", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        times.put("Default Zone", ZoneId.systemDefault().toString());
        
        // Test JWT time generation
        times.put("Date.from(Instant.now())", Date.from(instant).toString());
        
        return times;
    }
}
```

Test it:
```bash
curl http://localhost:8080/api/debug/time
```

## Solution 3: Adjust for Timezone Difference

If your server is in a different timezone than expected:

```java
public String generateTokenWithTimezone(String username, String[] roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("preferred_username", username);
    claims.put("email", username + "@example.com");
    
    String secret = "mySuperSecretKeyThatIsAtLeast32BytesLong123!";
    var key = Keys.hmacShaKeyFor(secret.getBytes());
    
    // Calculate with explicit timezone adjustment
    Instant now = Instant.now();
    
    // If you need to adjust for a specific timezone
    // ZoneOffset offset = ZoneOffset.ofHours(1); // For UTC+1
    // Instant adjustedNow = now.plusSeconds(offset.getTotalSeconds());
    
    return Jwts.builder()
            .claims(claims)
            .subject(username)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(3600)))
            .issuer("http://todo-api")
            .signWith(key)
            .compact();
}
```

## Solution 4: Complete Working Example with Time Fix

Here's a complete service that ensures correct time handling:

```java
package jaeger.de.miel.TodoAPI.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTimeService {
    
    @Value("${jwt.secret:mySuperSecretKeyThatIsAtLeast32BytesLong123!}")
    private String secret;
    
    public String generateTokenWithCorrectTime(String username, String[] roles) {
        // Log time information for debugging
        logTimeInfo();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("preferred_username", username);
        claims.put("email", username + "@example.com");
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        // Use Instant for UTC time
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(3600); // 1 hour later
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .issuer("http://todo-api")
                .signWith(key)
                .compact();
    }
    
    private void logTimeInfo() {
        Instant instant = Instant.now();
        Date date = new Date();
        ZoneId zone = ZoneId.systemDefault();
        
        System.out.println("=== JWT Time Debug ===");
        System.out.println("Instant.now(): " + instant);
        System.out.println("new Date(): " + date);
        System.out.println("Date.toInstant(): " + date.toInstant());
        System.out.println("System Zone: " + zone);
        System.out.println("Instant at zone: " + instant.atZone(zone));
        System.out.println("=========================");
    }
    
    // Alternative: Use specific time
    public String generateTokenWithSpecificTime(String username, String[] roles, 
                                                Instant issuedAt, long durationSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("preferred_username", username);
        claims.put("email", username + "@example.com");
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusSeconds(durationSeconds)))
                .issuer("http://todo-api")
                .signWith(key)
                .compact();
    }
}
```

## Solution 5: Check Your System Time

The issue might be with your operating system time:

### On Windows:
```cmd
# Check system time
time /t
date /t

# Check if using UTC
w32tm /tz

# Sync with time server
w32tm /resync
```

### On Linux/Mac:
```bash
# Check system time
date
date -u  # UTC time

# Check timezone
timedatectl status

# Set to UTC if needed
sudo timedatectl set-timezone UTC
```

## Solution 6: Verify the Token is Actually 1 Hour Off

Create a verification endpoint:

```java
@RestController
@RequestMapping("/api/debug")
public class TokenTimeController {
    
    @PostMapping("/verify-token-time")
    public Map<String, Object> verifyTokenTime(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Decode without validation to see times
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(payload, Map.class);
            
            // Get times from token
            Long iatSeconds = claims.get("iat") instanceof Integer ? 
                    ((Integer) claims.get("iat")).longValue() : (Long) claims.get("iat");
            Long expSeconds = claims.get("exp") instanceof Integer ? 
                    ((Integer) claims.get("exp")).longValue() : (Long) claims.get("exp");
            
            Instant iatInstant = Instant.ofEpochSecond(iatSeconds);
            Instant expInstant = Instant.ofEpochSecond(expSeconds);
            Instant now = Instant.now();
            
            result.put("token_iat", iatInstant);
            result.put("token_exp", expInstant);
            result.put("server_now", now);
            result.put("token_age_seconds", now.getEpochSecond() - iatSeconds);
            result.put("time_until_expiry_seconds", expSeconds - now.getEpochSecond());
            result.put("token_valid", now.isAfter(iatInstant) && now.isBefore(expInstant));
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
```

Test with:
```bash
curl -X POST http://localhost:8080/api/debug/verify-token-time \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_JWT_TOKEN"}'
```

## Important: Your Token Times ARE Correct!

Looking at your output:
- `"iat": "2025-12-30T23:59:56Z"` (issued at)
- `"exp": "2025-12-31T00:59:56Z"` (expires at)

**This is exactly 1 hour difference (3600 seconds), which is correct!** The `Z` means UTC time. If you're seeing this as "1 hour behind", it might be because:

1. **Your local timezone is UTC+1** (like Europe/London, Europe/Paris, etc.)
2. **You're comparing to local time instead of UTC**

The token expiration is working correctly. If you want the token to expire in 1 hour **from your local perspective**, you need to ensure your server's clock is set correctly.

## Quick Fix: Test with Different Duration

If you want to be sure, test with a very short token:

```java
// Create 2-minute token for testing
public String generateShortTestToken(String username, String[] roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("preferred_username", username);
    
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    
    Instant now = Instant.now();
    
    return Jwts.builder()
            .claims(claims)
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(120))) // 2 minutes
            .issuer("http://todo-api")
            .signWith(key)
            .compact();
}
```

Then test immediately and after 2 minutes to see if it expires correctly.

**Bottom line**: Your token generation code is correct. The times in UTC are accurate. If you're seeing a 1-hour difference, it's likely your local timezone vs UTC.