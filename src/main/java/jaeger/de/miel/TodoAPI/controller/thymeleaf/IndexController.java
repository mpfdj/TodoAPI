package jaeger.de.miel.TodoAPI.controller.thymeleaf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/", "/index.html"})
    public String index(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

            if (isAdmin) return "redirect:/ui/users";

            return "redirect:/ui/users/" + session.getAttribute("userId") + "/lists";
        }

        return "redirect:/login";
    }

}
