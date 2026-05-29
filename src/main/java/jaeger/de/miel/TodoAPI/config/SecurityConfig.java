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
//                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/ui/users", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }


//    @Bean
//    public AuthenticationSuccessHandler successHandler() {
//        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
//            System.out.println("✅ LOGIN SUCCESSFUL!");
//            System.out.println("   Username: " + authentication.getName());
//            System.out.println("   Authorities: " + authentication.getAuthorities());
//            response.sendRedirect("/ui/users");
//        };
//    }
//
//    @Bean
//    public AuthenticationFailureHandler failureHandler() {
//        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
//            System.out.println("❌ LOGIN FAILED!");
//            System.out.println("   Username attempted: " + request.getParameter("username"));
//            System.out.println("   Error: " + exception.getMessage());
//            response.sendRedirect("/login?error=true");
//        };
//    }






















    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println("Creating hardcoded user...");

        UserDetails user = User.builder()
                .username("admin@example.com")
                .password(passwordEncoder().encode("admin123"))
                .roles("USER")
                .build();

        System.out.println("User created: admin@example.com / admin123");

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}