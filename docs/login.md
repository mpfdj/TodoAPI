De `login` method in de LoginController moet gewoon de naam van de template teruggeven (`"login"`). De `defaultSuccessUrl` in de SecurityConfig wordt **automatisch** uitgevoerd door Spring Security, niet door de controller.

## Het antwoord:

```java
@GetMapping("/login")
public String login(...) {
    // Deze code wordt alleen uitgevoerd bij GET /login
    // De defaultSuccessUrl wordt afgehandeld door Spring Security na een succesvolle POST /login
    return "login";
}
```

## Hoe het werkt:

```
GET /login
    ↓
LoginController.login() → retourneert "login" template
    ↓
Gebruiker ziet login pagina
    ↓
Gebruiker vult email + wachtwoord in en klikt op submit
    ↓
POST /login (wordt NIET door LoginController afgehandeld!)
    ↓
Spring Security vangt POST /login op (UsernamePasswordAuthenticationFilter)
    ↓
Spring Security valideert credentials
    ↓
    ├─ Succes → defaultSuccessUrl("/ui/users", true) → redirect naar /ui/users
    └─ Fout → failureUrl("/login?error=true") → redirect naar /login?error=true
                ↓
                LoginController.login(error=true) → toont login pagina met error melding
```

## Waarom werkt dit?

De `@GetMapping("/login")` in je controller is **alleen voor GET requests**. Spring Security heeft zijn eigen filter (`UsernamePasswordAuthenticationFilter`) die **POST /login** afhandelt. Dit is standaard Spring Security gedrag.

## Wat je NIET moet doen:

```java
// Dit is FOUT - dit overschrijft Spring Security's POST /login handler
@PostMapping("/login")  
public String loginPost(...) {
    // Dit zal de defaultSuccessUrl OVERSCHRIJVEN!
    return "redirect:/somewhere";
}
```

## Volledig werkende code:

### LoginController (zoals je hebt - is al correct):
```java
package jaeger.de.miel.TodoAPI.controller.thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String registered,
                        Model model) {

        if (error != null) model.addAttribute("error", true);
        if (logout != null) model.addAttribute("logout", true);
        if (registered != null) model.addAttribute("registered", true);

        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("error", true);
        return "login";
    }
}
```

### SecurityConfig (moet UserDetailsService hebben):
```java
package jaeger.de.miel.TodoAPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/ui/users", true)  // ← Werkt automatisch!
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("test@example.com")
                .password(passwordEncoder().encode("test123"))
                .roles("USER")
                .build();
        
        return new InMemoryUserDetailsManager(user);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Samenvatting:

| Component | Verantwoordelijkheid |
|-----------|---------------------|
| **LoginController (GET /login)** | Toont de login pagina |
| **Spring Security (POST /login)** | Verwerkt de login en voert `defaultSuccessUrl` uit |
| **LoginController (GET /login?error=true)** | Toont login pagina met foutmelding |

**Conclusie:** Jouw LoginController is correct. De `defaultSuccessUrl` wordt automatisch uitgevoerd door Spring Security, zolang je maar een `UserDetailsService` en `PasswordEncoder` hebt geconfigureerd!