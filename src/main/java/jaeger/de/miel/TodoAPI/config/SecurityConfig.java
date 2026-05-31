package jaeger.de.miel.TodoAPI.config;

import jaeger.de.miel.TodoAPI.security.CustomUserDetails;
import jaeger.de.miel.TodoAPI.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, UserDetailsService userDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/register", "/css/**", "/js/**", "/h2-console/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(customSuccessHandler())
                    .failureUrl("/login?error=true")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll()
            )

            .rememberMe(rememberMe -> rememberMe
                    .rememberMeParameter("remember-me")  // Naam van de checkbox
                    .rememberMeCookieName("remember-me-cookie")
                    .tokenValiditySeconds(604800)  // 7 dagen (7 * 24 * 3600)
                    .key("uniqueAndSecretKeyForRememberMe")
                    .userDetailsService(userDetailsService))


            .sessionManagement(session -> session
                    .maximumSessions(1)
                    .sessionRegistry(sessionRegistry())
            )
            .userDetailsService(customUserDetailsService)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));  // Voor H2 console

        return http.build();
    }


    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();  // Cast naar CustomUserDetails om userId te krijgen
                Long userId = userDetails.getUserId();  // ← Geen UserService nodig!
//                String email = userDetails.getUsername();

                // Check of er een redirect URL in de session staat
                HttpSession session = request.getSession();
                String redirectUrl = (String) session.getAttribute("redirectAfterLogin");

                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    session.removeAttribute("redirectAfterLogin");  // Redirect naar de opgeslagen URL
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
        };
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        authenticationManagerBuilder.authenticationProvider(authProvider);

        return authenticationManagerBuilder.build();
    }


    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }


    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


//    // Extends / overrides CLASS BCryptPasswordEncoder not allowed to override a final method. This is not working. Implement the interface instead...
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder() {
//            @Override
//            public boolean matches(CharSequence rawPassword, String encodedPassword) {
//                System.out.println("═══════════════════════════════════════════════════");
//                System.out.println("PASSWORDENCODER WORDT AANGEROEPEN!");
//                System.out.println("Raw password uit formulier: " + rawPassword);
//                System.out.println("Encoded password uit database: " + encodedPassword);
//                System.out.println("───────────────────────────────────────────────────");
//
//                boolean result = super.matches(rawPassword, encodedPassword);
//
//                System.out.println("Resultaat: " + (result ? "MATCH!" : "GEEN MATCH!"));
//                System.out.println("═══════════════════════════════════════════════════");
//
//                return result;
//            }
//        };
//    }



//    // Implements INTERFACE PasswordEncoder
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new PasswordEncoder() {
//            private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
//
//            @Override
//            public String encode(CharSequence rawPassword) {
//                return delegate.encode(rawPassword);
//            }
//
//            @Override
//            public boolean matches(CharSequence rawPassword, String encodedPassword) {
//                System.out.println("═══════════════════════════════════════════════════");
//                System.out.println("PASSWORDENCODER WORDT AANGEROEPEN!");
//                System.out.println("Raw password uit formulier: " + rawPassword);
//                System.out.println("Encoded password uit database: " + encodedPassword);
//                System.out.println("───────────────────────────────────────────────────");
//
//                boolean result = delegate.matches(rawPassword, encodedPassword);
//
//                System.out.println("Resultaat: " + (result ? "MATCH!" : "GEEN MATCH!"));
//                System.out.println("═══════════════════════════════════════════════════");
//
//                return result;
//            }
//        };
//    }


}