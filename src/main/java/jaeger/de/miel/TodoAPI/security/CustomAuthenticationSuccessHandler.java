package jaeger.de.miel.TodoAPI.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        String name = userDetails.getName();
        Boolean isAdmin = userDetails.getIsAdmin();

        session.setAttribute("userId", userId);
        session.setAttribute("email", email);
        session.setAttribute("name", name);
        session.setAttribute("isAdmin", isAdmin);


        // Check of er een redirect URL in de session staat
        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            session.removeAttribute("redirectAfterLogin");
            response.sendRedirect(redirectUrl);
        }
        else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/ui/users");
        }
        else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
            response.sendRedirect("/ui/users/"+ userId + "/lists");
        }
        else {
            response.sendRedirect("/");
        }
    }
}