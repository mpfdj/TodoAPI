Hier is een compleet voorbeeld van een `UsernamePasswordAuthenticationFilter` die valideert tegen een H2 database:

## 1. Entity klasse (AppUser.java)

```java
package jaeger.de.miel.TodoAPI.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_user")
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "is_administrator", nullable = false)
    private boolean isAdministrator = false;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public boolean isAdministrator() { return isAdministrator; }
    public void setAdministrator(boolean administrator) { isAdministrator = administrator; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

## 2. Repository (AppUserRepository.java)

```java
package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

## 3. Custom UserDetailsService (CustomUserDetailsService.java)

```java
package jaeger.de.miel.TodoAPI.security;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        String role = appUser.isAdministrator() ? "ADMIN" : "USER";
        
        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + role))
                .build();
    }
}
```

## 4. Custom Authentication Filter (CustomAuthenticationFilter.java)

```java
package jaeger.de.miel.TodoAPI.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/login");  // Login endpoint
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws AuthenticationException {
        
        String email = request.getParameter("username");
        String password = request.getParameter("password");
        
        System.out.println("========================================");
        System.out.println("CustomAuthenticationFilter: Attempting login");
        System.out.println("Email: " + email);
        System.out.println("========================================");
        
        UsernamePasswordAuthenticationToken authRequest = 
                new UsernamePasswordAuthenticationToken(email, password);
        
        setDetails(request, authRequest);
        
        return this.getAuthenticationManager().authenticate(authRequest);
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        System.out.println("✅ Login successful for: " + authResult.getName());
        try {
            response.sendRedirect("/ui/users");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) {
        System.out.println("❌ Login failed: " + failed.getMessage());
        try {
            response.sendRedirect("/login?error=true");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 5. Security Config (SecurityConfig.java)

```java
package jaeger.de.miel.TodoAPI.config;

import jaeger.de.miel.TodoAPI.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        // Custom filter toevoegen
        CustomAuthenticationFilter customFilter = new CustomAuthenticationFilter(authenticationManager);
        customFilter.setFilterProcessesUrl("/login");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())  // Disable default form login
                .addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 6. Login Controller (LoginController.java)

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
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }
}
```

## 7. Data Initializer (DataInitializer.java)

```java
package jaeger.de.miel.TodoAPI.config;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(AppUserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                // Admin user
                AppUser admin = new AppUser();
                admin.setEmail("admin@example.com");
                admin.setName("Administrator");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setAdministrator(true);
                userRepository.save(admin);
                
                // Normal user
                AppUser user = new AppUser();
                user.setEmail("user@example.com");
                user.setName("Normal User");
                user.setPasswordHash(passwordEncoder.encode("user123"));
                user.setAdministrator(false);
                userRepository.save(user);
                
                System.out.println("========================================");
                System.out.println("✅ Test users created in H2 database:");
                System.out.println("Admin: admin@example.com / admin123");
                System.out.println("User: user@example.com / user123");
                System.out.println("========================================");
            }
        };
    }
}
```

## 8. Login HTML (login.html)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login</title>
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container">
    <div class="row justify-content-center min-vh-100 align-items-center">
        <div class="col-md-4">
            <div class="card shadow">
                <div class="card-header bg-primary text-white">
                    <h2 class="h5 mb-0">Login</h2>
                </div>
                <div class="card-body">
                    <div th:if="${error}" class="alert alert-danger">Invalid credentials</div>
                    <div th:if="${logout}" class="alert alert-info">Logged out successfully</div>
                    
                    <form th:action="@{/login}" method="post">
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" name="username" class="form-control" required autofocus>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Password</label>
                            <input type="password" name="password" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">Login</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

## Flow diagram:

```
User submits login form (POST /login)
         ↓
CustomAuthenticationFilter.attemptAuthentication()
         ↓
UsernamePasswordAuthenticationToken created
         ↓
AuthenticationManager.authenticate()
         ↓
DaoAuthenticationProvider (via CustomUserDetailsService)
         ↓
AppUserRepository.findByEmail(email)  ← H2 Database query
         ↓
PasswordEncoder.matches(raw, hash)    ← BCrypt check
         ↓
    ┌────┴────┐
    ↓         ↓
  Success   Failure
    ↓         ↓
redirect   redirect
to         to
/ui/users  /login?error=true
```

Dit is een compleet werkend voorbeeld van een authenticatie filter die valideert tegen een H2 database! 🎯