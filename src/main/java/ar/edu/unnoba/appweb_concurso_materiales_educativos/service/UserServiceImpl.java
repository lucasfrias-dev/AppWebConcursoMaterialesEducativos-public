package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.ResourceNotFoundException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.UserAlreadyExistsException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.EvaluacionRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.MaterialRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.UserRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.security.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           MaterialRepository materialRepository,
                           EvaluacionRepository evaluacionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUserDetails securityUserDetails) {
            return securityUserDetails.getUser();
        }
        if (principal instanceof User user) {
            return user;
        }
        return userRepository.findByEmail(authentication.getName());
    }

    /**
     * Carga detalles de un usuario basado en su nombre de usuario (correo electrónico).
     *
     * @param username El nombre de usuario (correo electrónico) del usuario a cargar.
     * @return Detalles del usuario envueltos en SecurityUserDetails.
     * @throws UsernameNotFoundException Si no se encuentra un usuario con el nombre de usuario proporcionado.
     * @throws DisabledException Si la cuenta del usuario está desactivada.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("El usuario con email " + username + " no existe");
        }

        if (!user.isActive()) {
            throw new DisabledException("Esta cuenta ha sido desactivada");
        }

        return new SecurityUserDetails(user);
    }

    /**
     * Crea un nuevo usuario con el rol especificado.
     *
     * @param user El nuevo usuario que se va a crear.
     * @param rol El rol que se asignará al nuevo usuario.
     * @return El usuario creado.
     */
    @Override
    @Transactional
    public User createUser(User user, User.Rol rol) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserAlreadyExistsException("El correo electrónico " + user.getEmail() + " ya está registrado en el sistema.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRol(rol);
        user.setActive(true);

        return userRepository.save(user);
    }

    /**
     * Recupera un usuario por su identificación.
     *
     * @param id La identificación del usuario a recuperar.
     * @return El usuario asociado a la identificación proporcionada.
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario con ID " + id + " no existe."));
    }

    /**
     * Asigna un material a un evaluador específico.
     *
     * @param materialId La identificación del material a asignar.
     * @param evaluadorId La identificación del evaluador al cual asignar el material.
     */
    @Override
    @Transactional
    public void asignarMaterialAEvaluador(Long materialId, Long evaluadorId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el material con ID: " + materialId));

        User evaluador = userRepository.findById(evaluadorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el evaluador con ID: " + evaluadorId));

        if (material.getEvaluadores().contains(evaluador)) {
            throw new IllegalStateException("El material ya se encuentra asignado a este evaluador.");
        }

        material.getEvaluadores().add(evaluador);
        evaluador.getMaterialesAEvaluar().add(material);

        materialRepository.save(material);
        userRepository.save(evaluador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllEvaluadores() {
        return userRepository.findAllEvaluadores();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllAdministradores() {
        return userRepository.findAllAdministradores();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getEvaluadoresPendientes(Material material) {
        return userRepository.findEvaluadoresPendientesByMaterial(material);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean haEvaluadoMaterial(User usuario, Material material) {
        Evaluacion evaluacion = evaluacionRepository.findByEvaluadorAndMaterial(usuario, material);
        return evaluacion != null;
    }

    @Override
    @Transactional
    public void bajaUsuario(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (user.getRol() != User.Rol.EVALUADOR && user.getRol() != User.Rol.CONCURSANTE) {
            throw new IllegalArgumentException("Solo los evaluadores o concursantes pueden ser dados de baja.");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void altaUsuario(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUsuarioConMaterialesAsignados(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        user.getMaterialesAEvaluar().size(); // Inicializar colección perezosa dentro de la transacción
        return user;
    }
}
