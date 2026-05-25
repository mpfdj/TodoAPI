package jaeger.de.miel.TodoAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/ui/users";
    }

    @GetMapping("/index.html")
    public String indexHtml() {
        return "redirect:/ui/users";
    }

}
