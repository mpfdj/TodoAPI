package jaeger.de.miel.TodoAPI.controller.thymeleaf;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DebugSessionController {

    @GetMapping("/debug/session")
    public String debugSession(HttpSession session, Model model) {
        // Basis sessie informatie
        Date creationTime = new Date(session.getCreationTime());
        Date lastAccessedTime = new Date(session.getLastAccessedTime());

        // Bereken verlooptijd
        long expirationTimeMillis = session.getLastAccessedTime() + (session.getMaxInactiveInterval() * 1000L);
        Date expirationTime = new Date(expirationTimeMillis);

        model.addAttribute("sessionId", session.getId());
        model.addAttribute("creationTime", creationTime);
        model.addAttribute("lastAccessedTime", lastAccessedTime);
        model.addAttribute("expirationTime", expirationTime);
        model.addAttribute("maxInactiveInterval", session.getMaxInactiveInterval());
        model.addAttribute("isNew", session.isNew());

        // Alle sessie attributen
        Map<String, Object> sessionAttributes = new HashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            sessionAttributes.put(name, value);
        }
        model.addAttribute("attributes", sessionAttributes);

        return "debug/session";
    }

    @GetMapping("/debug/session/clear")
    public String clearSession(HttpSession session) {
        session.invalidate();
        return "redirect:/debug/session";
    }

    @PostMapping("/debug/session/add-attribute")
    public String addAttribute(HttpSession session,
                               @RequestParam String key,
                               @RequestParam String value) {
        session.setAttribute(key, value);
        return "redirect:/debug/session";
    }

    @PostMapping("/debug/session/remove-attribute")
    public String removeAttribute(HttpSession session,
                                  @RequestParam String key) {
        session.removeAttribute(key);
        return "redirect:/debug/session";
    }
}