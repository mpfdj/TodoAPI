Dit is een **excellente vraag**! Het laat zien hoe Spring Security **achter de schermen** de formuliergegevens naar de `PasswordEncoder` brengt.

## De volledige route van formulier naar PasswordEncoder:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│          VAN HTML FORMULIER NAAR PASSWORDENCODER - DE VOLLEDIGE ROUTE        │
└─────────────────────────────────────────────────────────────────────────────┘

1. HTML FORMULIER (login.html)
   ┌─────────────────────────────────────┐
   │ <form th:action="@{/login}" method="post"> │
   │   <input type="email" name="email" />     │
   │   <input type="password" name="password" />│
   │ </form>                            │
   └─────────────────────────────────────┘
                    ↓
2. BROWSER stuurt HTTP POST request
   ┌─────────────────────────────────────┐
   │ POST /login HTTP/1.1               │
   │ Content-Type: application/x-www-form-urlencoded │
   │                                     │
   │ email=admin@example.com&password=admin123 │
   └─────────────────────────────────────┘
                    ↓
3. SPRING SECURITY filters
   ┌─────────────────────────────────────┐
   │ UsernamePasswordAuthenticationFilter │
   │ - Leest parameters uit request      │
   │ - Haalt "email" en "password" op    │
   └─────────────────────────────────────┘
                    ↓
4. UsernamePasswordAuthenticationToken
   ┌─────────────────────────────────────┐
   │ new UsernamePasswordAuthenticationToken( │
   │   email,    ← "admin@example.com"   │
   │   password  ← "admin123"            │
   │ )                                   │
   └─────────────────────────────────────┘
                    ↓
5. AuthenticationManager
   ┌─────────────────────────────────────┐
   │ Geeft token door aan provider       │
   └─────────────────────────────────────┘
                    ↓
6. DaoAuthenticationProvider
   ┌─────────────────────────────────────┐
   │ 1. Roept loadUserByUsername(email) aan │
   │    → Haalt gehashed password uit DB │
   │                                      │
   │ 2. Roept passwordEncoder.matches(   │
   │      rawPassword,    ← "admin123"   │
   │      encodedPassword ← "$2a$10$..." │
   │    )                                │
   └─────────────────────────────────────┘
```

## Gedetailleerde code-analyse:

### Stap 1: HTML Formulier
```html
<form th:action="@{/login}" method="post">
    <input type="email" name="email" value="admin@example.com" />
    <input type="password" name="password" value="admin123" />
    <button type="submit">Login</button>
</form>
```

### Stap 2: Spring Security leest de parameters

```java
// UsernamePasswordAuthenticationFilter (interne Spring Security code)
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, 
                                                HttpServletResponse response) {
        
        // HIER WORDEN DE PARAMETERS UIT HET FORMULIER GELEZEN!
        String username = obtainUsername(request);   // ← "admin@example.com"
        String password = obtainPassword(request);   // ← "admin123"
        
        UsernamePasswordAuthenticationToken authRequest = 
            new UsernamePasswordAuthenticationToken(username, password);
        
        return this.getAuthenticationManager().authenticate(authRequest);
    }
    
    protected String obtainUsername(HttpServletRequest request) {
        // Standaard kijkt hij naar parameter "username"
        return request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
    }
    
    protected String obtainPassword(HttpServletRequest request) {
        // Standaard kijkt hij naar parameter "password"
        return request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
    }
}
```

### Stap 3: Je kunt de parameter namen aanpassen in SecurityConfig

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")    // ← Zegt: gebruik "email" ipv "username"
                .passwordParameter("password") // ← Zegt: gebruik "password"
                .defaultSuccessUrl("/ui/users", true)
                .failureUrl("/login?error=true")
                .permitAll()
            );
        return http.build();
    }
}
```

### Stap 4: Hoe de PasswordEncoder de parameters krijgt

```java
// DaoAuthenticationProvider (interne Spring Security code)
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) {
        
        // HIER KOMT HET INGEVULDE PASSWORD BINNEN!
        String presentedPassword = authentication.getCredentials().toString(); // "admin123"
        
        // HIER KOMT DE PASSWORDENCODER IN ACTIE!
        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
```

## Visuele weergave van de data flow:

```
HTML FORMULIER
┌──────────────────────────────────────────────┐
│                                              │
│  ┌────────────────────────────────────┐      │
│  │ Email: admin@example.com            │      │
│  └────────────────────────────────────┘      │
│                    ↓                         │
│  ┌────────────────────────────────────┐      │
│  │ Password: admin123                  │      │
│  └────────────────────────────────────┘      │
│                    ↓                         │
│           [SUBMIT] button                    │
└──────────────────────────────────────────────┘
                    ↓
        HTTP POST REQUEST
┌──────────────────────────────────────────────┐
│ POST /login HTTP/1.1                        │
│ Content-Type: application/x-www-form-urlencoded│
│                                              │
│ email=admin%40example.com&password=admin123  │
└──────────────────────────────────────────────┘
                    ↓
    UsernamePasswordAuthenticationFilter
┌──────────────────────────────────────────────┐
│ request.getParameter("email")                │
│ → "admin@example.com"                        │
│                                              │
│ request.getParameter("password")             │
│ → "admin123"                                 │
└──────────────────────────────────────────────┘
                    ↓
    UsernamePasswordAuthenticationToken
┌──────────────────────────────────────────────┐
│ new UsernamePasswordAuthenticationToken(     │
│   "admin@example.com",                       │
│   "admin123"                                 │
│ )                                            │
└──────────────────────────────────────────────┘
                    ↓
    DaoAuthenticationProvider
┌──────────────────────────────────────────────┐
│ authentication.getCredentials()              │
│ → "admin123"                                 │
│                                              │
│ userDetails.getPassword()                    │
│ → "$2a$10$NkMZJ5YlqTqQqJqJqJqJqOhVQxY..."   │
│                                              │
│ passwordEncoder.matches(                     │
│   "admin123",                                │
│   "$2a$10$NkMZJ5YlqTqQqJqJqJqJqOhVQxY..."   │
│ )                                            │
│ → true                                       │
└──────────────────────────────────────────────┘
```

## Complete debug voorbeeld:

```java
package jaeger.de.miel.TodoAPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder() {
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                System.out.println("═══════════════════════════════════════════════════");
                System.out.println("🔐 PASSWORDENCODER WORDT AANGEROEPEN!");
                System.out.println("📝 Raw password uit formulier: " + rawPassword);
                System.out.println("🔑 Encoded password uit database: " + encodedPassword);
                System.out.println("───────────────────────────────────────────────────");
                
                boolean result = super.matches(rawPassword, encodedPassword);
                
                System.out.println("✅ Resultaat: " + (result ? "MATCH! ✅" : "GEEN MATCH! ❌"));
                System.out.println("═══════════════════════════════════════════════════");
                
                return result;
            }
        };
    }
}
```

## Belangrijk om te weten:

| Element | Waar komt het vandaan? |
|---------|----------------------|
| **email/username** | Uit `request.getParameter("email")` of `"username"` |
| **password** | Uit `request.getParameter("password")` |
| **gehashed password** | Uit de database via `loadUserByUsername()` |
| **passwordEncoder** | Uit de `@Bean` die jij hebt gemaakt |

## Conclusie:

De `username` en `password` worden uit het HTML formulier gehaald door de **`UsernamePasswordAuthenticationFilter`**. Deze filter leest de HTTP request parameters en stopt ze in een `Authentication` token. Vervolgens gebruikt de `DaoAuthenticationProvider` deze token om de `PasswordEncoder` aan te roepen met het ingevulde wachtwoord en de hash uit de database.

**Jij hoeft niets te doen!** Spring Security regelt dit allemaal automatisch achter de schermen. 🎯