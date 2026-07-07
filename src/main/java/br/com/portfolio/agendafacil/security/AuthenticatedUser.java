package br.com.portfolio.agendafacil.security;

import br.com.portfolio.agendafacil.domain.User;
import br.com.portfolio.agendafacil.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {

    private final User user;

    public AuthenticatedUser(User user) {
        this.user = user;
    }

    public Long id() {
        return user.getId();
    }

    public String name() {
        return user.getName();
    }

    public String email() {
        return user.getEmail();
    }

    public UserRole role() {
        return user.getRole();
    }

    public User user() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
