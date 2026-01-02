package jaeger.de.miel.TodoAPI.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class SecurityTestController_2 {

    // Test 1: Using hasRole (requires ROLE_ prefix)
    @GetMapping("/has-role-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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