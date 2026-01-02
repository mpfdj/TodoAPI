package jaeger.de.miel.TodoAPI.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SecurityTestController_1 {

    // Public endpoint
    @GetMapping("/public/message")
    public String publicMessage() {
        return "This is public";
    }

    // Protected endpoint with any authenticated user
    @GetMapping("/user/profile")
    public String userProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return "Hello, " + username;
    }

    // Role-based protection
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(@AuthenticationPrincipal Jwt jwt) {
        return "Admin dashboard - Welcome " + jwt.getClaimAsString("preferred_username");
    }

    // Multiple roles
    @GetMapping("/manager/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String managerReports() {
        return "Manager reports";
    }
}