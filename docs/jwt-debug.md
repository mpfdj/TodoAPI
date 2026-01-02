The `@PreAuthorize("hasRole('ADMIN')")` not working is a common Spring Security issue. Here's how to diagnose and fix it:

## 1. **Diagnose the Issue First**

Create a debug endpoint to see what authorities/roles your JWT actually has:

```java
package jaeger.de.miel.TodoAPI.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class SecurityDebugController {
    
    // Public endpoint to check roles
    @GetMapping("/roles")
    public Map<String, Object> checkRoles(@AuthenticationPrincipal Jwt jwt, 
                                          Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. What's in the JWT token
        result.put("jwt_subject", jwt.getSubject());
        result.put("jwt_claims", jwt.getClaims());
        
        List<String> rolesFromJwt = jwt.getClaimAsStringList("roles");
        result.put("jwt_roles_claim", rolesFromJwt);
        
        // 2. What Spring Security sees
        if (authentication != null) {
            result.put("authentication_name", authentication.getName());
            result.put("authentication_authorities", 
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            result.put("authentication_isAuthenticated", authentication.isAuthenticated());
            result.put("authentication_class", authentication.getClass().getName());
        }
        
        // 3. Check if has ADMIN role via different methods
        result.put("hasRole_ADMIN_direct", 
            authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        
        return result;
    }
    
    // Test @PreAuthorize
    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String testAdmin() {
        return "You have ADMIN role!";
    }
    
    // Alternative test
    @GetMapping("/test-admin-alt")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String testAdminAlt() {
        return "You have ROLE_ADMIN authority!";
    }
}
```

Test it:
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/debug/roles
```

## 2. **Common Fixes**

### Fix 1: Missing `@EnableMethodSecurity` Annotation

**This is the most common cause!** Make sure your `SecurityConfig` has:

```java
package jaeger.de.miel.TodoAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // <<< THIS IS CRITICAL
public class SecurityConfig {
    // ... rest of your config
}
```

### Fix 2: Wrong Authority Prefix Configuration

Update your `JwtAuthenticationConverter`:

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    
    // CRITICAL: Extract from "roles" claim
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    
    // CRITICAL: Set the prefix (or remove it if needed)
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); 
    // OR if your JWT already has "ROLE_" prefix:
    // grantedAuthoritiesConverter.setAuthorityPrefix("");
    
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    
    // Optional: Set principal claim name
    jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
    
    return jwtAuthenticationConverter;
}
```

### Fix 3: Complete Working SecurityConfig

Here's a complete, working configuration:

```java
package jaeger.de.miel.TodoAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    
    @Value("${jwt.secret:mySuperSecretKeyThatIsAtLeast32BytesLong123!}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**", "/api/debug/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(
            Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("http://todo-api"));
        
        return decoder;
    }
    
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                String principal = jwt.getClaimAsString("preferred_username");
                if (principal == null) {
                    principal = jwt.getSubject();
                }
                return new JwtAuthenticationToken(jwt, authorities, principal);
            }
            
            private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
                // Extract roles from JWT claims
                List<String> roles = jwt.getClaimAsStringList("roles");
                
                if (roles != null) {
                    return roles.stream()
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                
                // Fallback to scopes or other claims
                List<String> scopes = jwt.getClaimAsStringList("scope");
                if (scopes != null) {
                    return scopes.stream()
                            .map(scope -> "SCOPE_" + scope)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                
                return List.of();
            }
        };
    }
}
```

## 3. **Alternative: Use Method Security with Different Annotations**

Try different annotations to see what works:

```java
@RestController
@RequestMapping("/api/test")
public class SecurityTestController {
    
    // Test 1: Using hasRole (requires ROLE_ prefix)
    @GetMapping("/has-role-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String hasRoleAdmin() {
        return "hasRole('ADMIN') works!";
    }
    
    // Test 2: Using hasAuthority (exact authority name)
    @GetMapping("/has-authority-role-admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String hasAuthorityRoleAdmin() {
        return "hasAuthority('ROLE_ADMIN') works!";
    }
    
    // Test 3: Using @RolesAllowed (JSR-250)
    @GetMapping("/jsr-admin")
    @jakarta.annotation.security.RolesAllowed("ADMIN")
    public String jsrAdmin() {
        return "@RolesAllowed('ADMIN') works!";
    }
    
    // Test 4: Direct authority check
    @GetMapping("/direct-check")
    @PreAuthorize("isAuthenticated()")
    public String directCheck(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.contains("ADMIN")) {
            return "Direct check: You have ADMIN in JWT claims";
        }
        return "Direct check: No ADMIN in JWT claims";
    }
}
```

## 4. **Test Script to Diagnose**

Create a test script:

```bash
#!/bin/bash

echo "=== Testing @PreAuthorize Configuration ==="
echo

# Get an ADMIN token
ADMIN_TOKEN=$(curl -s http://localhost:8080/api/public/tokens/admin | jq -r '.token')

echo "1. Testing /api/debug/roles:"
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/debug/roles | jq .
echo

echo "2. Testing hasRole('ADMIN'):"
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/test/has-role-admin \
  -w "HTTP Status: %{http_code}\n"
echo

echo "3. Testing hasAuthority('ROLE_ADMIN'):"
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/test/has-authority-role-admin \
  -w "HTTP Status: %{http_code}\n"
echo

echo "4. Testing @RolesAllowed('ADMIN'):"
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/test/jsr-admin \
  -w "HTTP Status: %{http_code}\n"
```

## 5. **Check Application Properties**

Ensure you have the right Spring Security version and dependencies:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-config</artifactId>
</dependency>
```

## 6. **Enable Debug Logging**

Add to `application.yml`:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.access.method: DEBUG
    org.springframework.security.access.intercept: DEBUG
    jaeger.de.miel.TodoAPI: DEBUG
```

Check logs for messages like:
```
Evaluating expression "hasRole('ADMIN')"
Authorization successful
```
or
```
Authorization failed: Access is denied
```

## 7. **Quick Diagnostic Steps**

1. **First, verify `@EnableMethodSecurity` is present**
2. **Check the debug endpoint** `/api/debug/roles` to see what authorities Spring sees
3. **Try different annotations** (`hasRole` vs `hasAuthority`)
4. **Check JWT claims** - ensure "roles" claim exists with ["USER", "ADMIN"]
5. **Verify authority prefix** - Spring adds "ROLE_" by default

## Most Likely Solution

Based on your symptoms, **99% chance it's one of these**:

1. **Missing `@EnableMethodSecurity`** - Add it to SecurityConfig
2. **Wrong authority mapping** - Use the custom `jwtAuthenticationConverter` above
3. **JWT doesn't have "roles" claim** - Verify your token generator

**Try this minimal fix first:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Add this line
public class SecurityConfig {
    // ... existing code
}
```

Then test with the debug endpoint. If you still get 403, share the output of `/api/debug/roles` and I can provide a more specific fix.