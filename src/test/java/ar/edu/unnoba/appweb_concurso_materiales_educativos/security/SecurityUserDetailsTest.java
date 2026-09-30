package ar.edu.unnoba.appweb_concurso_materiales_educativos.security;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUserDetailsTest {

    @Test
    void testSecurityUserDetailsDelegatesProperly() {
        User user = new User();
        user.setId(10L);
        user.setEmail("test@unnoba.edu.ar");
        user.setPassword("EncodedPass");
        user.setNombre("Test");
        user.setApellido("User");
        user.setRol(User.Rol.EVALUADOR);
        user.setActive(true);

        SecurityUserDetails userDetails = new SecurityUserDetails(user);

        assertSame(user, userDetails.getUser());
        assertEquals("test@unnoba.edu.ar", userDetails.getUsername());
        assertEquals("EncodedPass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_EVALUADOR", authorities.iterator().next().getAuthority());
    }

    @Test
    void testSecurityUserDetailsWithNullOrInactiveUser() {
        User user = new User();
        user.setActive(false);
        user.setRol(null);

        SecurityUserDetails userDetails = new SecurityUserDetails(user);
        assertFalse(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}
