Hier is een uitgebreide uitleg van hoe de validatie tegen de database werkt:

## Stap-voor-stap uitleg:

### 1. **De Service annotatie**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService
```
- `@Service` maakt dit een Spring bean die automatisch wordt ontdekt
- `UserDetailsService` is een interface die Spring Security gebruikt om gebruikers op te halen

### 2. **De constructor injectie**
```java
private final UserRepository userRepository;

public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```
- Spring injecteert automatisch de `UserRepository`
- `final` zorgt dat de repository niet kan worden gewijzigd

### 3. **De loadUserByUsername methode**
```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
```
- Dit is de **belangrijkste methode** die Spring Security aanroept
- Spring Security geeft het email adres (ingevuld in login formulier) als parameter
- Deze methode wordt **automatisch** aangeroepen tijdens het inloggen

### 4. **Database query**
```java
AppUser appUser = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
```
- `userRepository.findByEmail(email)` zoekt in de H2 database naar een gebruiker met dit email adres
- Als de gebruiker **niet** bestaat → `orElseThrow` gooit een exception
- Als de gebruiker **wel** bestaat → `AppUser` object wordt opgehaald

### 5. **Rol bepaling**
```java
String role = appUser.getIsAdministrator() ? "ADMIN" : "USER";
```
- Als `is_administrator = true` → rol = "ADMIN"
- Als `is_administrator = false` → rol = "USER"

### 6. **CustomUserDetails aanmaken**
```java
return new CustomUserDetails(
        appUser.getEmail(),           // username (email)
        appUser.getPasswordHash(),    // hashed wachtwoord uit database
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)),
        appUser.getId()               // userId voor later gebruik
);
```

## Het volledige validatie proces:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    LOGIN VALIDATIE FLOW                                  │
└─────────────────────────────────────────────────────────────────────────┘

1. Gebruiker vult login formulier in
   ┌─────────────────────────────────────┐
   │ Email: admin@example.com             │
   │ Password: admin123                   │
   └─────────────────────────────────────┘
                    ↓
2. Spring Security ontvangt POST /login
                    ↓
3. Spring Security roept loadUserByUsername(email) aan
                    ↓
4. CustomUserDetailsService loadUserByUsername(email)
                    ↓
5. Database query: SELECT * FROM app_user WHERE email = 'admin@example.com'
                    ↓
   ┌─────────────────────────────────────┐
   │ H2 Database                         │
   │ ┌─────────────────────────────────┐ │
   │ │ id=1, email=admin@example.com   │ │
   │ │ password_hash=$2a$10$...        │ │
   │ │ is_administrator=true           │ │
   │ └─────────────────────────────────┘ │
   └─────────────────────────────────────┘
                    ↓
6. Gebruiker gevonden? JA
                    ↓
7. PasswordEncoder vergelijkt:
   ┌─────────────────────────────────────┐
   │ admin123 (plain)                    │
   │         ↓                           │
   │ BCrypt.encode("admin123")           │
   │         ↓                           │
   │ $2a$10$NkMZJ5... (hash)             │
   │         ↓                           │
   │ Vergelijk met database hash          │
   └─────────────────────────────────────┘
                    ↓
   ┌─────────────────┬─────────────────┐
   │ Matches?        │ Action          │
   ├─────────────────┼─────────────────┤
   │ JA              │ ✅ Login succes │
   │ NEE             │ ❌ Login fout   │
   └─────────────────┴─────────────────┘
```

## Visualisatie van de data flow:

```java
// 1. Gebruiker logt in
Login Form: email="admin@example.com", password="admin123"
                    ↓
// 2. Spring Security roept aan
loadUserByUsername("admin@example.com")
                    ↓
// 3. Database query
SELECT * FROM app_user WHERE email = 'admin@example.com'
                    ↓
// 4. Resultaat uit database
AppUser {
    id: 1,
    email: "admin@example.com",
    passwordHash: "$2a$10$NkMZJ5YlqTqQqJqJqJqJqOhVQxYxYxYxYxYxYxYxYxYxYxYxYxYxY",
    isAdministrator: true
}
                    ↓
// 5. Spring Security vergelijkt
PasswordEncoder.matches("admin123", databaseHash) → true
                    ↓
// 6. CustomUserDetails wordt teruggegeven
CustomUserDetails {
    username: "admin@example.com",
    password: "$2a$10$NkMZJ5...",
    authorities: [ROLE_ADMIN],
    userId: 1
}
                    ↓
// 7. Login succes! → Redirect naar success URL
```

## Wat gebeurt er als de gebruiker niet bestaat?

```java
// 1. Database query
AppUser appUser = userRepository.findByEmail("unknown@example.com")
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    ↓
// 2. Exception wordt gegooid
UsernameNotFoundException: "User not found with email: unknown@example.com"
                    ↓
// 3. Spring Security vangt dit op
                    ↓
// 4. Redirect naar /login?error=true
```

## De SecurityConfig gebruikt deze service:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;  // ← Hier wordt jouw service gebruikt
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)  // ← Spring Security gebruikt deze service
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/ui/users", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")  // ← email wordt gebruikt als username
                .passwordParameter("password")
                .permitAll()
            );
        
        return http.build();
    }
}
```

## Samenvatting:

| Stap | Wie | Wat |
|------|-----|-----|
| 1 | Gebruiker | Vult email + wachtwoord in |
| 2 | Spring Security | Ontvangt POST /login |
| 3 | `CustomUserDetailsService` | Haalt gebruiker uit database via email |
| 4 | `UserRepository` | Voert SQL query uit op H2 database |
| 5 | `PasswordEncoder` | Vergelijkt ingevuld wachtwoord met database hash |
| 6 | Spring Security | Beslist of login succesvol is |
| 7 | `AuthenticationSuccessHandler` | Bepaalt waar gebruiker naartoe gaat |

**De kern:** `CustomUserDetailsService` is de **brug** tussen Spring Security en jouw H2 database. Het haalt de gebruiker op en Spring Security doet de wachtwoord validatie! 🎯