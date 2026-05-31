package jaeger.de.miel.TodoAPI.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long userId;
    private final String email;
    private final String name;
    private final Boolean isAdmin;

    public CustomUserDetails(String username,
                             String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long userId,
                             String email,
                             String name,
                             Boolean isAdmin) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.isAdmin = isAdmin;
    }

}