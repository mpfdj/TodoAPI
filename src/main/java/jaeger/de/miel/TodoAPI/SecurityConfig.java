package jaeger.de.miel.TodoAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // CRITICAL for @PreAuthorize to work
public class SecurityConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;


    // 1) Special chain for H2 console
    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/h2-console/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Let H2 console use an HttpSession, independent from the API being stateless
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        // No resource server or custom auth on this chain
        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**", "/api/debug/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }


    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] decodedKey = Base64.getDecoder().decode(
                Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }


    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                String principal = jwt.getClaimAsString("preferred_username");
                if (principal == null) {
                    principal = jwt.getSubject();
                }
                return new JwtAuthenticationToken(jwt, authorities, principal);
            }

            private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
                // Extract roles from JWT claims
                List<String> roles = jwt.getClaimAsStringList("roles");

                if (roles != null) {
                    return roles.stream()
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                // Fallback to scopes or other claims
                List<String> scopes = jwt.getClaimAsStringList("scope");
                if (scopes != null) {
                    return scopes.stream()
                            .map(scope -> "SCOPE_" + scope)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                return List.of();
            }
        };
    }

}
