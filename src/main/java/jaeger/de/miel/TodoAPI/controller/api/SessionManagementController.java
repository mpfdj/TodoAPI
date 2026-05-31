package jaeger.de.miel.TodoAPI.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SessionManagementController {

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    @GetMapping("/actuator/sessions")
    public Map<String, Object> getActiveSessions() {
        Map<String, Object> result = new HashMap<>();

        if (sessionRegistry == null) {
            result.put("error", "SessionRegistry is not configured");
            result.put("message", "Add sessionManagement with sessionRegistry to SecurityConfig");
            return result;
        }

        List<Map<String, Object>> activeSessions = new ArrayList<>();

        List<Object> principals = sessionRegistry.getAllPrincipals();
        result.put("totalPrincipals", principals.size());

        for (Object principal : principals) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("principal", principal.toString());
                sessionInfo.put("sessionId", session.getSessionId());
                sessionInfo.put("lastRequest", session.getLastRequest());
                sessionInfo.put("expired", session.isExpired());
                activeSessions.add(sessionInfo);
            }
        }

        result.put("activeSessions", activeSessions);
        result.put("totalSessions", activeSessions.size());

        return result;
    }
}