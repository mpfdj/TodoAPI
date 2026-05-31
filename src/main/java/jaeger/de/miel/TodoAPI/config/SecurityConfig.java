package jaeger.de.miel.TodoAPI.config;

import jaeger.de.miel.TodoAPI.security.CustomAuthenticationSuccessHandler;
import jaeger.de.miel.TodoAPI.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler customSuccessHandler;


    public SecurityConfig(CustomUserDetailsService customUserDetailsService, UserDetailsService userDetailsService, CustomAuthenticationSuccessHandler customSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.userDetailsService = userDetailsService;
        this.customSuccessHandler = customSuccessHandler;
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
                    .successHandler(customSuccessHandler)
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
    public AuthenticationManager authenticationManager(HttpSecurity http) {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

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