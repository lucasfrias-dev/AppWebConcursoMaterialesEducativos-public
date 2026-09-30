package ar.edu.unnoba.appweb_concurso_materiales_educativos.security;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador que implementa {@link UserDetails} de Spring Security, desacoplando
 * la entidad de dominio {@link User} del framework de seguridad.
 */
public class SecurityUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;

    public SecurityUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null || user.getRol() == null) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()));
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getEmail() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.isActive();
    }
}
