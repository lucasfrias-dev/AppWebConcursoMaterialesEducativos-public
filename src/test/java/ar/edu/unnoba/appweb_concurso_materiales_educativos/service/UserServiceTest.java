package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.UserAlreadyExistsException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User.Rol;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.EvaluacionRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.MaterialRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setNombre("Juan");
        user.setApellido("Perez");
        user.setEmail("juan@example.com");
        user.setPassword("Secret123");
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findByEmail("juan@example.com")).thenReturn(null);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user, Rol.CONCURSANTE);

        assertNotNull(created);
        assertEquals("juan@example.com", created.getEmail());
        assertEquals("hashedPassword", created.getPassword());
        assertEquals(Rol.CONCURSANTE, created.getRol());
        assertTrue(created.isActive());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmailThrowsException() {
        User existing = new User();
        existing.setEmail("juan@example.com");
        when(userRepository.findByEmail("juan@example.com")).thenReturn(existing);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(user, Rol.CONCURSANTE));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoadUserByUsername_Success() {
        user.setRol(Rol.CONCURSANTE);
        user.setActive(true);
        when(userRepository.findByEmail("juan@example.com")).thenReturn(user);

        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername("juan@example.com");

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof ar.edu.unnoba.appweb_concurso_materiales_educativos.security.SecurityUserDetails);
        assertEquals("juan@example.com", userDetails.getUsername());
        assertEquals("Secret123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CONCURSANTE")));
    }

    @Test
    void testLoadUserByUsername_NotFoundThrowsException() {
        when(userRepository.findByEmail("inexistente@example.com")).thenReturn(null);

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("inexistente@example.com"));
    }

    @Test
    void testLoadUserByUsername_DisabledThrowsException() {
        user.setActive(false);
        when(userRepository.findByEmail("juan@example.com")).thenReturn(user);

        assertThrows(org.springframework.security.authentication.DisabledException.class,
                () -> userService.loadUserByUsername("juan@example.com"));
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("juan@example.com")).thenReturn(user);

        User found = userService.findByEmail("juan@example.com");
        assertNotNull(found);
        assertEquals("juan@example.com", found.getEmail());
    }

    @Test
    void testGetAuthenticatedUser_WithSecurityUserDetails() {
        user.setRol(Rol.ADMINISTRADOR);
        ar.edu.unnoba.appweb_concurso_materiales_educativos.security.SecurityUserDetails userDetails =
                new ar.edu.unnoba.appweb_concurso_materiales_educativos.security.SecurityUserDetails(user);
        org.springframework.security.core.Authentication auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        User resolved = userService.getAuthenticatedUser(auth);
        assertNotNull(resolved);
        assertEquals("juan@example.com", resolved.getEmail());
    }

    @Test
    void testGetAuthenticatedUser_WithUserPrincipal() {
        org.springframework.security.core.Authentication auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());

        User resolved = userService.getAuthenticatedUser(auth);
        assertNotNull(resolved);
        assertEquals("juan@example.com", resolved.getEmail());
    }

    @Test
    void testGetAuthenticatedUser_WithNullAuth() {
        assertNull(userService.getAuthenticatedUser(null));
    }
}
