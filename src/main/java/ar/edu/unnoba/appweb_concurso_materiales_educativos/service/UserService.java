package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    // Recupera un usuario por su correo electrónico.
    User findByEmail(String email);

    // Obtiene el usuario autenticado a partir del objeto Authentication de Spring Security.
    User getAuthenticatedUser(Authentication authentication);

    // Recupera una lista de todos los evaluadores registrados en el sistema.
    List<User> getAllEvaluadores();

    // Recupera una lista de todos los administradores registrados en el sistema.
    List<User> getAllAdministradores();

    // Crea un nuevo usuario con el rol especificado.
    User createUser(User user, User.Rol rol);

    // Recupera un usuario por su identificación.
    User findById(Long id);

    // Asigna un material a un evaluador específico.
    void asignarMaterialAEvaluador(Long materialId, Long evaluadorId);

    // Recupera una lista de evaluadores pendientes de evaluar para un material específico.
    List<User> getEvaluadoresPendientes(Material material);

    // Verifica si un usuario ha evaluado un material específico.
    boolean haEvaluadoMaterial(User usuario, Material material);

    // Desactiva un usuario en el sistema.
    void bajaUsuario(Long id);

    // Activa un usuario en el sistema.
    void altaUsuario(Long id);

    // Recupera una lista de todos los usuarios registrados en el sistema.
    List<User> findAll();

    User getUsuarioConMaterialesAsignados(Long userId);
}
