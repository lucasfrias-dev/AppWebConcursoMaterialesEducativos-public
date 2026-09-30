package ar.edu.unnoba.appweb_concurso_materiales_educativos.controller;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.ConcursoDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.UserRegistrationDto;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.UserAlreadyExistsException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.ConcursoService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.EvaluacionService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.MaterialService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.UserService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.email.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/administrador")
@PreAuthorize("hasRole('ADMINISTRADOR')") /* solo los administradores acceden a este controlador */
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final MaterialService materialService;
    private final ConcursoService concursoService;
    private final EvaluacionService evaluacionService;
    private final EmailService emailService;

    public AdminController(UserService userService, MaterialService materialService, ConcursoService concursoService,
            EvaluacionService evaluacionService, EmailService emailService) {
        this.userService = userService;
        this.materialService = materialService;
        this.concursoService = concursoService;
        this.evaluacionService = evaluacionService;
        this.emailService = emailService;
    }

    /**
     * Controlador para mostrar el panel del administrador en la vista.
     * Este método maneja las solicitudes GET dirigidas a "/index".
     *
     * @param authentication Información de autenticación del usuario.
     * @param model          Modelo utilizado para pasar datos a la vista.
     * @param error          Mensaje de error a mostrar en la vista.
     * @param message        Mensaje informativo a mostrar en la vista.
     * @return El nombre de la vista que muestra el panel del administrador.
     */
    @GetMapping("/index")
    public String dashboardAdministrador(Authentication authentication, Model model,
            @ModelAttribute("error") String error, @ModelAttribute("message") String message) {
        // Obtiene el usuario autenticado desde la información de autenticación.
        User usuario = userService.getAuthenticatedUser(authentication);

        // Agrega el usuario al modelo para que esté disponible en la vista.
        model.addAttribute("usuario", usuario);

        // Agrega el concurso actual al modelo para que esté disponible en la vista.
        model.addAttribute("concursoActual", concursoService.getConcursoActual());

        // Agrega los concursos finalizados al modelo para que estén disponibles en la
        // vista.
        List<Concurso> concursosFinalizados = concursoService.getAllConcursosFinalizados();
        model.addAttribute("concursosFinalizados", concursosFinalizados);

        // Agrega el último concurso finalizado al modelo para que esté disponible en la
        // vista.
        model.addAttribute("ultimoConcursoFinalizado", concursoService.getUltimoConcursoFinalizado());

        // Agrega un mensaje de error al modelo para que esté disponible en la vista.
        model.addAttribute("error", error);
        // Agrega un mensaje al modelo para que esté disponible en la vista.
        model.addAttribute("message", message);

        // Devuelve el nombre de la vista que mostrará el panel del administrador.
        return "administrador/panel-administrador";
    }

    /**
     * Controlador para la página de cierre de sesión ("/logout").
     *
     * @param request  La solicitud HTTP.
     * @param response La respuesta HTTP.
     * @return Una redirección a la página de inicio de sesión con un parámetro
     *         indicando el cierre de sesión.
     */
    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        // Obtiene la información de autenticación del contexto de seguridad.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si hay un usuario autenticado, realiza el proceso de cierre de sesión.
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        // Redirige a la página principal.
        return "redirect:/";
    }

    /**
     * Controlador para mostrar el formulario de creación de un nuevo concurso en la
     * vista.
     * Este método maneja las solicitudes GET dirigidas a "/new-concurso".
     *
     * @param model El modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista que muestra el formulario de creación de un
     *         nuevo concurso.
     */
    @GetMapping("/new-concurso")
    public String ShowCreateConcurso(Model model) {
        // Crea una nueva instancia de ConcursoDTO
        ConcursoDTO concursoDTO = new ConcursoDTO();
        // Genera y establece el número de edición del concurso
        String edicion = concursoService.generateEdicion();
        concursoDTO.setEdicion(edicion);
        // Agrega el DTO y la edición calculada al modelo para que estén
        // disponibles en la vista
        model.addAttribute("concurso", concursoDTO);
        model.addAttribute("nextEdicion", edicion);
        // Devuelve el nombre de la vista que mostrará el formulario de creación de un
        // nuevo concurso
        return "administrador/new-concurso";
    }

    /**
     * Controlador para crear un nuevo concurso.
     * Este método maneja las solicitudes POST dirigidas a "/new-concurso".
     *
     * @param concursoDTO        El DTO ConcursoDTO a crear y validar, obtenido del
     *                           formulario.
     * @param result             El resultado de la validación del objeto ConcursoDTO.
     * @param model              El modelo utilizado para pasar datos a la vista en
     *                           caso de error.
     * @param redirectAttributes Los atributos de redirección para agregar mensajes
     *                           flash.
     * @return Una redirección a la página de inicio del administrador si el
     *         concurso se crea con éxito,
     *         de lo contrario, muestra nuevamente el formulario de creación de
     *         concurso con mensajes de error.
     */
    @PostMapping("/new-concurso")
    public String createConcurso(@Valid @ModelAttribute("concurso") ConcursoDTO concursoDTO, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        // Verifica si hay errores de validación en el objeto ConcursoDTO.
        if (result.hasErrors()) {
            if (concursoDTO.getEdicion() == null || concursoDTO.getEdicion().isBlank()) {
                concursoDTO.setEdicion(concursoService.generateEdicion());
            }
            model.addAttribute("nextEdicion", concursoDTO.getEdicion());
            // Si hay errores, vuelve a mostrar el formulario de creación de concurso con
            // los errores.
            model.addAttribute("concurso", concursoDTO);
            return "administrador/new-concurso";
        }

        // Si no hay errores de validación, convierte el DTO a entidad y crea el concurso.
        Concurso concurso = concursoDTO.toEntity();
        concursoService.createConcurso(concurso);

        // Agrega un mensaje de éxito como atributo flash para que esté disponible
        // después de la redirección.
        redirectAttributes.addFlashAttribute("success", "Concurso creado exitosamente");

        // Redirige al usuario a la página de inicio del administrador.
        return "redirect:/administrador/index";
    }

    /**
     * Controlador para mostrar los detalles de un concurso específico.
     * Este método maneja las solicitudes GET dirigidas a "/concurso/{edicion}".
     *
     * @param edicion La edición del concurso que se desea mostrar.
     * @param model   El modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista que muestra los detalles del concurso
     *         específico.
     */
    @GetMapping("/concurso/{edicion}")
    public String showConcurso(@PathVariable String edicion, Model model) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        // Obtiene el concurso específico por su edición utilizando el servicio
        // correspondiente.
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);
        // Agrega el concurso al modelo para que esté disponible en la vista.
        model.addAttribute("concurso", concurso);

        // Devuelve el nombre de la vista que muestra los detalles del concurso
        // específico.
        return "administrador/detalle-concurso";
    }

    /**
     * Controlador para mostrar el formulario de edición de una fecha específica de
     * un concurso.
     * Este método maneja las solicitudes GET dirigidas a
     * "/concurso/{edicion}/editar/{campo}".
     *
     * @param edicion La edición del concurso del cual se desea editar la fecha.
     * @param campo   El campo de fecha que se desea editar.
     * @param model   El modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista que muestra el formulario de edición de fecha.
     */
    @GetMapping("/concurso/{edicion}/editar/{campo}")
    public String editFecha(@PathVariable String edicion, @PathVariable String campo, Model model) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        // Obtiene el concurso específico por su edición utilizando el servicio
        // correspondiente.
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);
        // Agrega el concurso al modelo para que esté disponible en la vista.
        model.addAttribute("concurso", concurso);
        // Agrega el campo de fecha que se desea editar al modelo.
        model.addAttribute("campo", campo);

        // Devuelve el nombre de la vista que muestra el formulario de edición de fecha.
        return "administrador/edit-fecha";
    }

    /**
     * Controlador para actualizar una fecha específica de un concurso.
     * Este método maneja las solicitudes POST dirigidas a
     * "/concurso/{edicion}/editar/{campo}".
     *
     * @param edicion            La edición del concurso del cual se desea
     *                           actualizar la fecha.
     * @param campo              El campo de fecha que se desea actualizar.
     * @param fecha              La nueva fecha a establecer.
     * @param redirectAttributes Los atributos de redirección para agregar mensajes
     *                           flash.
     * @return Una redirección a la página de detalles del concurso actualizado.
     */
    @PostMapping("/concurso/{edicion}/editar/{campo}")
    public String updateFecha(@PathVariable String edicion, @PathVariable String campo,
            @RequestParam("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fecha,
            RedirectAttributes redirectAttributes) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        // Actualiza la fecha del concurso utilizando el servicio correspondiente.
        concursoService.updateFecha(edicion, campo, fecha);
        // Agrega un mensaje de éxito como atributo flash para que esté disponible
        // después de la redirección.
        redirectAttributes.addFlashAttribute("success", "Fecha actualizada exitosamente");
        // Redirige al usuario a la página de detalles del concurso actualizado.
        return "redirect:/administrador/concurso/" + edicion.replace(" ", "-");
    }

    /**
     * Controlador para cerrar un concurso específico.
     * Este método maneja las solicitudes POST dirigidas a
     * "/concurso/{edicion}/cerrar".
     *
     * @param edicion            La edición del concurso a cerrar, obtenida como
     *                           parte de la URL.
     * @param redirectAttributes Los atributos de redirección para agregar mensajes
     *                           flash.
     * @return Una redirección a la página de inicio del administrador con un
     *         mensaje de éxito si el concurso se cierra con éxito,
     *         de lo contrario, redirige con un mensaje de error.
     */
    @PostMapping("/concurso/{edicion}/cerrar")
    public String cerrarConcurso(@PathVariable String edicion, RedirectAttributes redirectAttributes) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        try {
            // Intenta cerrar el concurso utilizando el servicio correspondiente.
            concursoService.cerrarConcurso(edicion);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Si se produce una excepción durante el cierre del concurso, agrega un mensaje
            // de error como atributo flash.
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Redirige al usuario a la página de inicio del administrador.
            return "redirect:/administrador/index";
        }
        // Si el concurso se cierra con éxito, agrega un mensaje de éxito como atributo
        // flash.
        redirectAttributes.addFlashAttribute("message", "Concurso cerrado exitosamente.");
        // Redirige al usuario a la página de inicio del administrador.
        return "redirect:/administrador/index";
    }

    /**
     * Controlador para mostrar el formulario de reabrir un concurso específico.
     * Este método maneja las solicitudes GET dirigidas a
     * "/concurso/{edicion}/reabrir".
     *
     * @param edicion La edición del concurso a reabrir, obtenida como parte de la
     *                URL.
     * @param model   El modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista que muestra el formulario de reabrir concurso.
     */
    @GetMapping("/concurso/{edicion}/reabrir")
    public String reabrirConcurso(@PathVariable String edicion, Model model) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        // Obtiene el concurso específico por su edición utilizando el servicio
        // correspondiente.
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);
        // Agrega el concurso al modelo para que esté disponible en la vista.
        model.addAttribute("concurso", concurso);
        // Devuelve el nombre de la vista que muestra el formulario de reabrir concurso.
        return "administrador/reabrir-concurso";
    }

    /**
     * Controlador para reabrir un concurso específico.
     * Este método maneja las solicitudes POST dirigidas a
     * "/concurso/{edicion}/reabrir".
     *
     * @param edicion            La edición del concurso a reabrir, obtenida como
     *                           parte de la URL.
     * @param fechaFin           La nueva fecha de finalización del concurso,
     *                           obtenida como parámetro de solicitud.
     * @param redirectAttributes Los atributos de redirección para agregar mensajes
     *                           flash.
     * @return Una redirección a la página de inicio del administrador con un
     *         mensaje de éxito si el concurso se reabre con éxito,
     *         de lo contrario, redirige con un mensaje de error.
     */
    @PostMapping("/concurso/{edicion}/reabrir")
    public String reabrirConcurso(@PathVariable String edicion, @RequestParam("fechaFin") LocalDateTime fechaFin,
            RedirectAttributes redirectAttributes) {
        // Reemplaza cualquier guión en el nombre de la edición con un espacio para
        // manejar correctamente los nombres compuestos.
        edicion = edicion.replace("-", " ");
        try {
            // Obtiene el último concurso finalizado y el concurso actual.
            Concurso ultimoConcursoFinalizado = concursoService.getUltimoConcursoFinalizado();
            Concurso concursoActual = concursoService.getConcursoActual();

            // Verifica si se puede reabrir el concurso.
            if (concursoActual != null
                    || (ultimoConcursoFinalizado != null && !ultimoConcursoFinalizado.getEdicion().equals(edicion))) {
                throw new IllegalStateException(
                        "No se puede reabrir el concurso. Solo se puede reabrir el último concurso finalizado y solo si no hay un concurso actual.");
            }

            // Reabre el concurso utilizando el servicio correspondiente.
            concursoService.reabrirConcurso(edicion, fechaFin);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Si se produce una excepción durante la reapertura del concurso, agrega un
            // mensaje de error como atributo flash.
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Redirige al usuario a la página de inicio del administrador.
            return "redirect:/administrador/index";
        }
        // Si el concurso se reabre con éxito, agrega un mensaje de éxito como atributo
        // flash.
        redirectAttributes.addFlashAttribute("message", "Concurso reabierto exitosamente.");
        // Redirige al usuario a la página de inicio del administrador.
        return "redirect:/administrador/index";
    }

    /**
     * Controlador para mostrar todos los materiales asociados a un concurso
     * específico.
     * Este método maneja las solicitudes GET dirigidas a
     * "/materiales/{edicion}/all".
     *
     * @param model   El modelo utilizado para pasar datos a la vista.
     * @param edicion La edición del concurso para la cual se mostrarán los
     *                materiales, obtenida como parte de la URL.
     * @return El nombre de la vista que muestra todos los materiales asociados al
     *         concurso.
     */
    @GetMapping("/materiales/{edicion}/all")
    public String showMateriales(Model model, @PathVariable String edicion) {
        edicion = edicion.replace("-", " ");
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);
        List<Material> materiales = materialService.getMaterialesByConcurso(concurso);
        model.addAttribute("materiales", materiales);
        model.addAttribute("edicion", edicion);
        return "administrador/materiales";
    }

    @Transactional(readOnly = true)
    @GetMapping("/materiales/{edicion}/{materialId}")
    public String showMaterial(@PathVariable String edicion, @PathVariable Long materialId, Model model) {
        edicion = edicion.replace("-", " ");
        Material material = materialService.getMaterial(materialId);
        if (material == null) {
            return "redirect:/administrador/materiales/" + edicion.replace(" ", "-") + "/all";
        }
        List<Evaluacion> evaluaciones = evaluacionService.getEvaluacionesByMaterial(material);
        Set<User> evaluadores = Set.of();
        if (material.getEvaluadores() != null) {
            material.getEvaluadores().size();
            evaluadores = material.getEvaluadores();
        }

        model.addAttribute("material", material);
        model.addAttribute("evaluadores", evaluadores);
        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("edicion", edicion);
        return "administrador/detalle-material";
    }

    /**
     * Controlador para mostrar todos los materiales pendientes de aprobación en la
     * interfaz de administrador.
     * Este método maneja las solicitudes GET dirigidas a
     * "/materiales/{edicion}/pendientes-de-aprobacion".
     *
     * @param model   El modelo que se utilizará para pasar datos a la vista.
     * @param edicion La edición del concurso para la cual se mostrarán los
     *                materiales pendientes de aprobación, obtenida como parte de la
     *                URL.
     * @return El nombre de la vista que mostrará la lista de materiales pendientes
     *         de aprobación.
     */
    @GetMapping("/materiales/{edicion}/pendientes-de-aprobacion")
    public String showMaterialesPendientesAprobacion(Model model, @PathVariable String edicion) {
        // Agrega la lista de materiales pendientes de aprobación al modelo para que
        // esté disponible en la vista.
        model.addAttribute("materiales", materialService.getMaterialesPendientesAprobacion());
        model.addAttribute("edicion", edicion.replace("-", " "));

        // Devuelve el nombre de la vista que mostrará la lista de materiales pendientes
        // de aprobación.
        return "administrador/materiales-pendientes-aprobacion";
    }

    /**
     * Controlador para mostrar todos los materiales pendientes de evaluación en la
     * interfaz de administrador.
     * Incluye información sobre los evaluadores pendientes asociados a cada
     * material.
     *
     * @param model   El modelo que se utilizará para pasar datos a la vista.
     * @param edicion La edición del concurso para la cual se mostrarán los
     *                materiales pendientes de evaluación, obtenida como parte de la
     *                URL.
     * @return El nombre de la vista que mostrará la lista de materiales pendientes
     *         de evaluación.
     */
    @GetMapping("/materiales/{edicion}/pendientes-de-evaluacion")
    public String showMaterialesPendientesEvaluacion(Model model, @PathVariable String edicion) {
        edicion = edicion.replace("-", " ");
        // Obtiene la lista de materiales pendientes de evaluación.
        List<Material> materiales = materialService.getMaterialesPendientesEvaluacion();

        // Crea un mapa para asociar cada material con la lista de evaluadores
        // pendientes para ese material.
        Map<Material, List<User>> evaluadoresPendientes = new HashMap<>();
        if (materiales != null) {
            for (Material material : materiales) {
                evaluadoresPendientes.put(material, userService.getEvaluadoresPendientes(material));
            }
        }

        // Agrega la lista de materiales y el mapa de evaluadores pendientes al modelo
        // para que estén disponibles en la vista.
        model.addAttribute("materiales", materiales);
        model.addAttribute("evaluadoresPendientes", evaluadoresPendientes);
        model.addAttribute("edicion", edicion);

        // Devuelve el nombre de la vista que mostrará la lista de materiales pendientes
        // de evaluación.
        return "administrador/materiales-pendientes-evaluacion";
    }

    /**
     * Controlador para aprobar un material específico.
     *
     * @param id La identificación del material a aprobar, obtenida del path de la
     *           URL.
     * @return Una redirección a la página que muestra la lista de materiales
     *         pendientes de aprobación.
     */
    @PostMapping("/materiales/{id}/aprobar")
    public String aprobarMaterial(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Aprobar el material utilizando el servicio materialService.
        materialService.aprobarMaterial(id);
        redirectAttributes.addFlashAttribute("message", "Material aprobado exitosamente.");

        // Obtiene el concurso actual para construir la URL de redirección.
        Concurso concurso = concursoService.getConcursoActual();
        // Reemplaza los espacios en blanco en la edición del concurso con guiones para
        // construir la URL.
        String edicion = concurso != null ? concurso.getEdicion().replace(" ", "-") : "III-Edicion";
        // Redirige a la página que muestra la lista de materiales pendientes de
        // aprobación.
        return "redirect:/administrador/materiales/" + edicion + "/pendientes-de-aprobacion";
    }

    /**
     * Controlador para rechazar un material específico.
     *
     * @param id La identificación del material a rechazar, obtenida del path de la
     *           URL.
     * @return Una redirección a la página que muestra la lista de materiales
     *         pendientes de aprobación.
     */
    @PostMapping("/materiales/{id}/rechazar")
    public String rechazarMaterial(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Rechazar el material utilizando el servicio materialService.
        materialService.rechazarMaterial(id);
        redirectAttributes.addFlashAttribute("message", "Material rechazado.");

        // Obtiene el concurso actual para construir la URL de redirección.
        Concurso concurso = concursoService.getConcursoActual();
        // Reemplaza los espacios en blanco en la edición del concurso con guiones para
        // construir la URL.
        String edicion = concurso != null ? concurso.getEdicion().replace(" ", "-") : "III-Edicion";
        // Redirige a la página que muestra la lista de materiales pendientes de
        // aprobación.
        return "redirect:/administrador/materiales/" + edicion + "/pendientes-de-aprobacion";
    }

    /**
     * Controlador para mostrar la página de registro de un nuevo evaluador.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la página de registro de un nuevo
     *         evaluador.
     */
    @GetMapping("/register/evaluador")
    public String registerEvaluador(Model model) {
        model.addAttribute("evaluador", new UserRegistrationDto());
        return "administrador/register-evaluador";
    }

    @PostMapping("/register/evaluador")
    public String createEvaluador(
            @Valid @ModelAttribute("evaluador") UserRegistrationDto dto,
            BindingResult result,
            ModelMap model,
            RedirectAttributes redirectAttributes) {
        if (dto.getPasswordConfirm() == null || !dto.getPassword().equals(dto.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "error.passwordConfirm", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "administrador/register-evaluador";
        }

        try {
            User user = dto.toEntity();

            userService.createUser(user, User.Rol.EVALUADOR);
            emailService.sendEmail(user.getEmail(), "Has sido dado de alta como Evaluador en el CED",
                    "¡Bienvenido a la plataforma de concursos de materiales educativos! Ya formas parte del equipo de evaluadores del CED.");
            redirectAttributes.addFlashAttribute("message", "Evaluador registrado exitosamente.");
            return "redirect:/administrador/evaluadores/registrados";
        } catch (UserAlreadyExistsException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "administrador/register-evaluador";
        }
    }

    @GetMapping("/register/admin")
    public String registerAdministrador(Model model) {
        model.addAttribute("admin", new UserRegistrationDto());
        return "administrador/register-admin";
    }

    @PostMapping("/register/admin")
    public String createAdministrador(
            @Valid @ModelAttribute("admin") UserRegistrationDto dto,
            BindingResult result,
            ModelMap model,
            RedirectAttributes redirectAttributes) {
        if (dto.getPasswordConfirm() == null || !dto.getPassword().equals(dto.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "error.passwordConfirm", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "administrador/register-admin";
        }

        try {
            User user = dto.toEntity();

            userService.createUser(user, User.Rol.ADMINISTRADOR);
            emailService.sendEmail(user.getEmail(), "Has sido dado de alta como Administrador en el CED",
                    "¡Bienvenido a la plataforma de concursos de materiales educativos! Ya formas parte del equipo de administradores del CED.");
            redirectAttributes.addFlashAttribute("message", "Administrador registrado exitosamente.");
            return "redirect:/administrador/admin/registrados";
        } catch (UserAlreadyExistsException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "administrador/register-admin";
        }
    }

    /**
     * Controlador para mostrar la lista de evaluadores registrados en la interfaz
     * de administrador.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la lista de evaluadores
     *         registrados.
     */
    @GetMapping("/evaluadores/registrados")
    public String showEvaludoresDisponibles(Model model) {
        // Agrega la lista de evaluadores registrados al modelo para que esté disponible
        // en la vista.
        model.addAttribute("evaluadores", userService.getAllEvaluadores());

        // Devuelve el nombre de la vista que mostrará la lista de evaluadores
        // registrados.
        return "administrador/evaluadores-registrados";
    }

    /**
     * Controlador para mostrar la lista de administradores registrados en la
     * interfaz de administrador.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la lista de administradores
     *         registrados.
     */
    @GetMapping("/admin/registrados")
    public String showAdministradoresDisponibles(Model model) {
        // Agrega la lista de administradores registrados al modelo para que esté
        // disponible en la vista.
        model.addAttribute("administradores", userService.getAllAdministradores());

        // Devuelve el nombre de la vista que mostrará la lista de administradores
        // registrados.
        return "administrador/admin-registrados";
    }

    /**
     * Controlador para mostrar la lista de todos los usuarios en la interfaz de
     * administrador.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la lista de todos los usuarios.
     */
    @GetMapping("/usuarios/all")
    public String showUsers(Model model) {
        // Agrega la lista de todos los usuarios al modelo para que esté disponible en
        // la vista.
        model.addAttribute("usuarios", userService.findAll());

        // Devuelve el nombre de la vista que mostrará la lista de todos los usuarios.
        return "administrador/all-users";
    }

    /**
     * Controlador para mostrar la página de baja de un usuario específico en la
     * interfaz de administrador.
     *
     * @param id    La identificación del usuario, obtenida del path de la URL.
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la página de baja de un usuario
     *         específico.
     */
    @GetMapping("/usuarios/{id}/bajaUsuario")
    public String showBajaUsuario(@PathVariable Long id, Model model) {
        // Obtiene el usuario con la identificación proporcionada.
        User usuario = userService.findById(id);

        // Agrega el usuario al modelo para que esté disponible en la vista.
        model.addAttribute("usuario", usuario);

        // Devuelve el nombre de la vista que mostrará la página de baja de un usuario
        // específico.
        return "administrador/baja-user";
    }

    /**
     * Controlador para procesar la baja de un usuario específico en la interfaz de
     * administrador.
     *
     * @param id La identificación del usuario a dar de baja, obtenida del path de
     *           la URL.
     * @return Una redirección a la página que muestra la lista de todos los
     *         usuarios.
     */
    @PostMapping("/usuarios/{id}/bajaUsuario")
    public String bajaUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Intenta realizar la baja del usuario.
        try {
            // Utiliza el servicio userService para dar de baja al usuario con el ID
            // especificado.
            userService.bajaUsuario(id);
        } catch (IllegalArgumentException e) {
            // Si ocurre un error (por ejemplo, se intenta dar de baja un usuario
            // administrador), se agrega un mensaje de error al modelo.
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // Redirige a la misma página de baja de usuario, mostrando el mensaje de error.
            return "redirect:/administrador/usuarios/" + id + "/bajaUsuario";
        }

        // Si la baja del usuario fue exitosa, se redirige a la página que muestra la
        // lista de todos los usuarios.
        return "redirect:/administrador/usuarios/all";
    }

    /**
     * Controlador para mostrar la página de alta de un usuario específico en la
     * interfaz de administrador.
     *
     * @param id    La identificación del usuario, obtenida del path de la URL.
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la página de alta de un usuario
     *         específico.
     */
    @GetMapping("/usuarios/{id}/altaUsuario")
    public String showAltaUsuario(@PathVariable Long id, Model model) {
        // Obtiene el usuario con la identificación proporcionada.
        User usuario = userService.findById(id);

        // Agrega el usuario al modelo para que esté disponible en la vista.
        model.addAttribute("usuario", usuario);

        // Devuelve el nombre de la vista que mostrará la página de alta de un usuario
        // específico.
        return "administrador/alta-user";
    }

    /**
     * Controlador para procesar el alta de un usuario específico en la interfaz de
     * administrador.
     *
     * @param id La identificación del usuario a dar de alta, obtenida del path de
     *           la URL.
     * @return Una redirección a la página que muestra la lista de todos los
     *         usuarios.
     */
    @PostMapping("/usuarios/{id}/altaUsuario")
    public String altaUsuario(@PathVariable Long id) {
        // Realiza el alta del usuario utilizando el servicio userService.
        userService.altaUsuario(id);

        // Redirige a la página que muestra la lista de todos los usuarios.
        return "redirect:/administrador/usuarios/all";
    }

    /**
     * Controlador para mostrar la página de asignación de material a evaluadores en
     * la interfaz de administrador.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que mostrará la página de asignación de
     *         material a evaluadores.
     */
    @GetMapping("/evaluadores/asignar-material")
    public String showAsignarMaterial(Model model) {
        // Agrega la lista de materiales y la lista de evaluadores al modelo para que
        // estén disponibles en la vista.
        model.addAttribute("materiales",
                materialService.getMaterialesParticipantesByConcurso(concursoService.getConcursoActual()));
        model.addAttribute("evaluadores", userService.getAllEvaluadores());

        // Devuelve el nombre de la vista que mostrará la página de asignación de
        // material a evaluadores.
        return "administrador/asignar-material-evaluador";
    }

    /**
     * Controlador para procesar la asignación de un material a un evaluador en la
     * interfaz de administrador.
     *
     * @param materialId  La identificación del material a asignar, obtenida del
     *                    path de la URL.
     * @param evaluadorId La identificación del evaluador al que se asignará el
     *                    material, obtenida del path de la URL.
     * @return Una respuesta ResponseEntity con un mensaje indicando el resultado de
     *         la asignación.
     */
    @PostMapping("/evaluadores/asignar-material")
    public String asignarMaterial(
            @RequestParam(value = "materialId", required = false) Long materialId,
            @RequestParam(value = "evaluatorId", required = false) Long evaluatorId,
            @RequestParam(value = "evaluadorId", required = false) Long evaluadorId,
            RedirectAttributes redirectAttributes) {
        Long targetEvaluadorId = evaluatorId != null ? evaluatorId : evaluadorId;
        if (materialId == null || targetEvaluadorId == null) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un material y un evaluador.");
            return "redirect:/administrador/evaluadores/asignar-material";
        }

        // Intenta asignar el material al evaluador.
        try {
            // Utiliza el servicio userService para asignar el material con el ID
            // especificado al evaluador con el ID especificado.
            userService.asignarMaterialAEvaluador(materialId, targetEvaluadorId);

            // Obtiene la dirección de correo electrónico y el nombre del evaluador.
            User evaluador = userService.findById(targetEvaluadorId);
            if (evaluador != null) {
                String email = evaluador.getEmail();
                String nombre = evaluador.getNombre() + " " + evaluador.getApellido();

                // Envía un correo electrónico al evaluador para notificarle la asignación del
                // material.
                emailService.sendEmail(email, "Asignación de material", "Estimado/a " + nombre + ",\n\n" +
                        "Se le ha asignado un material para su evaluación. Por favor, inicie sesión en la plataforma del CED "
                        +
                        "para revisar el material y realizar su evaluación.\n\n" +
                        "Atentamente,\n" +
                        "El equipo de Concurso de Materiales Educativos - CED");
            }

            redirectAttributes.addFlashAttribute("message", "Material asignado al evaluador exitosamente.");
        } catch (Exception e) {
            logger.error("Error al asignar material {} a evaluador {}: {}", materialId, targetEvaluadorId,
                    e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al asignar material: " + e.getMessage());
        }

        return "redirect:/administrador/evaluadores/asignar-material";
    }

    @PostMapping("/{materialId}/{evaluadorId}/asignar-material")
    public String asignarMaterialPath(
            @PathVariable("materialId") Long materialId,
            @PathVariable("evaluadorId") Long evaluadorId,
            RedirectAttributes redirectAttributes) {
        return asignarMaterial(materialId, evaluadorId, null, redirectAttributes);
    }

    /**
     * Controlador para mostrar la página de perfil del usuario autenticado en la
     * interfaz de administrador.
     *
     * @param model          El modelo que se utilizará para pasar datos a la vista.
     * @param authentication La información de autenticación que contiene al usuario
     *                       autenticado.
     * @return El nombre de la vista que mostrará la página de perfil del usuario.
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        // Obtiene al usuario autenticado desde la información de autenticación.
        User sessionUser = userService.getAuthenticatedUser(authentication);

        // Agrega el usuario al modelo para que esté disponible en la vista.
        model.addAttribute("user", sessionUser);

        // Devuelve el nombre de la vista que mostrará la página de perfil del usuario.
        return "administrador/profile";
    }

    /**
     * Controlador para mostrar los materiales aprobados en la interfaz de
     * administrador.
     *
     * @param model         El modelo que se utilizará para pasar datos a la vista.
     * @param edicion       La edición del concurso, obtenida del path de la URL.
     * @param redirectAttrs Atributos de redirección que pueden contener mensajes
     *                      para mostrar en la vista.
     * @param request       La solicitud HTTP que se está procesando.
     * @return El nombre de la vista que representa la lista de materiales
     *         participantes.
     */
    @GetMapping("/materiales/{edicion}/participantes")
    public String showMaterialesAprobados(Model model, @PathVariable String edicion, RedirectAttributes redirectAttrs,
            HttpServletRequest request) {
        // Verifica si la edición no es nula.
        if (!"null".equals(edicion)) {
            // Reemplaza los guiones con espacios en la edición.
            edicion = edicion.replace("-", " ");
            // Obtiene el concurso asociado a la edición desde el servicio concursoService.
            Concurso concurso = concursoService.getConcursoByEdicion(edicion);
            // Obtiene la lista de materiales participantes del concurso actual desde el
            // servicio materialService.
            List<Material> materialesParticipantes = materialService.getMaterialesParticipantesByConcurso(concurso);
            // Agrega la lista de materiales participantes al modelo para que esté
            // disponible en la vista.
            model.addAttribute("materialesParticipantes",
                    materialesParticipantes != null ? materialesParticipantes : List.of());
            // Obtiene la lista de materiales ganadores del concurso actual desde el
            // servicio materialService.
            List<Material> materialesGanadores = materialService.getMaterialesGanadores(concurso);
            // Agrega la lista de materiales ganadores al modelo para que esté disponible en
            // la vista.
            model.addAttribute("materialesGanadores", materialesGanadores != null ? materialesGanadores : List.of());
            // Agrega la edición al modelo para que esté disponible en la vista.
            model.addAttribute("edicion", edicion);

            // Crea un mapa para almacenar las evaluaciones por material.
            Map<Long, List<Evaluacion>> evaluacionesPorMaterial = new HashMap<>();
            // Itera sobre cada material para obtener sus evaluaciones y las almacena en el
            // mapa.
            if (materialesParticipantes != null) {
                for (Material material : materialesParticipantes) {
                    List<Evaluacion> evaluaciones = evaluacionService.getEvaluacionesByMaterial(material);
                    evaluacionesPorMaterial.put(material.getId(), evaluaciones);
                }
            }
            // Agrega las evaluaciones por material al modelo para que estén disponibles en
            // la vista.
            model.addAttribute("evaluacionesPorMaterial", evaluacionesPorMaterial);
        } else {
            // Si la edición es nula, agrega null al modelo para indicarlo.
            model.addAttribute("edicion", null);
            model.addAttribute("materialesParticipantes", List.of());
            model.addAttribute("materialesGanadores", List.of());
        }

        // Verifica si hay un mensaje de redirección y, si es así, lo agrega al modelo.
        if (redirectAttrs.getFlashAttributes().containsKey("message")) {
            model.addAttribute("message", redirectAttrs.getFlashAttributes().get("message"));
        }

        // Devuelve el nombre de la vista que representa la lista de materiales
        // participantes.
        return "administrador/materiales-participantes";
    }

    /**
     * Controlador para establecer un material como ganador.
     *
     * @param materialId    El ID del material que se establecerá como ganador,
     *                      obtenido del path de la URL.
     * @param edicion       La edición del concurso, obtenida del path de la URL.
     * @param redirectAttrs Atributos de redirección que pueden contener mensajes
     *                      para mostrar en la vista.
     * @return La URL de redirección a la página que muestra la lista de materiales
     *         participantes.
     */
    @PostMapping("/materiales/{edicion}/participantes/{idmaterial}/ganador")
    public String ganadorMaterial(@PathVariable("idmaterial") Long materialId, @PathVariable("edicion") String edicion,
            RedirectAttributes redirectAttrs) {

        // Establece el material como ganador.
        materialService.setMaterialGanador(materialId);
        // Agrega un mensaje de éxito como atributo flash para que esté disponible
        // después de la redirección.
        redirectAttrs.addFlashAttribute("message", "Material declarado como ganador con éxito");

        // Redirige a la página que muestra la lista de materiales participantes.
        return "redirect:/administrador/materiales/" + edicion + "/participantes";
    }

    /**
     * Controlador para deshacer un material como ganador.
     *
     * @param materialId El ID del material que se deshará como ganador, obtenido
     *                   del path de la URL.
     * @param edicion    La edición del concurso, obtenida del path de la URL.
     * @return La URL de redirección a la página que muestra la lista de materiales
     *         participantes.
     */
    @PostMapping("/materiales/{edicion}/participantes/{materialId}/remove-ganador")
    public String deshacerGanadorMaterial(@PathVariable("materialId") Long materialId,
            @PathVariable("edicion") String edicion) {
        // Se llama al servicio materialService para quitar el material como ganador.
        materialService.removeMaterialGanador(materialId);
        // Después de quitar el material como ganador, se redirige al usuario a la
        // página de materiales participantes de esa edición del concurso.
        return "redirect:/administrador/materiales/" + edicion + "/participantes";
    }
}
