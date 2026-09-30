package ar.edu.unnoba.appweb_concurso_materiales_educativos.controller;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.UserRegistrationDto;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.UserAlreadyExistsException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.ConcursoService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.MaterialService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.UserService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.email.EmailService;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.service.pdf.PdfService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {

    private final MaterialService materialService;
    private final UserService userService;
    private final ConcursoService concursoService;
    private final PdfService pdfService;
    private final EmailService emailService;

    public HomeController(MaterialService materialService, UserService userService, ConcursoService concursoService,
            PdfService pdfService, EmailService emailService) {
        this.materialService = materialService;
        this.userService = userService;
        this.concursoService = concursoService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    /**
     * Controlador para redirigir a la página principal del sistema.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que representa la página principal del sistema.
     */
    @GetMapping("/")
    public String home(Model model) {
        // Obtiene el ultimo concurso desde el servicio concursoService.
        Concurso concurso = concursoService.getUltimoConcurso();

        if (concurso != null) {
            // Agrega el ultimo concurso al modelo para que esté disponible en la vista.
            model.addAttribute("concurso", concurso);

            // Agrega el nombre de la edición del ultimo concurso al modelo para que esté
            // disponible en la vista.
            model.addAttribute("ultimaEdicion", concurso.getEdicion());

            // Agrega el año de finalización del ultimo concurso al modelo para que esté
            // disponible en la vista.
            model.addAttribute("anio", concurso.getFechaFin().getYear());
        }

        // Agrega la lista de concursos anteriores al modelo para que esté disponible en
        // la vista.
        // No incluye el ultimo concurso.
        model.addAttribute("concursosAnteriores", concursoService.getConcursosFinalizadosMenosUltimo());

        // Devuelve el nombre de la vista que representa la página principal del
        // sistema.
        return "index";
    }

    /**
     * Controlador para mostrar la página de inicio de sesión.
     *
     * @param error          El tipo de error que se produjo durante el inicio de
     *                       sesión.
     * @param model          El modelo que se utilizará para pasar datos a la vista.
     * @param authentication La autenticación del usuario que ha iniciado sesión.
     * @return El nombre de la vista que representa la página de inicio de sesión.
     */
    @GetMapping("/login")
    public String showLogin(@RequestParam(value = "error", required = false) String error, Model model,
            Authentication authentication) {
        // Si el usuario ya está autenticado, redirige a la página de inicio
        // correspondiente
        if (authentication != null && authentication.isAuthenticated()) {
            return defaultAfterLogin(authentication);
        }

        // Verifica el tipo de error y agrega un mensaje correspondiente al modelo.
        if ("disabled".equals(error)) {
            model.addAttribute("loginError", "Esta cuenta ha sido desactivada");
        } else if ("bad_credentials".equals(error)) {
            model.addAttribute("loginError", "Usuario o contraseña incorrectos");
        }

        // Agrega un objeto User al modelo para su uso en el formulario de inicio de
        // sesión.
        model.addAttribute("user", new User());

        // Devuelve el nombre de la vista que representa la página de inicio de sesión.
        return "login";
    }

    /**
     * Controlador para redirigir a la página por defecto después de un inicio de
     * sesión exitoso.
     *
     * @param authentication La autenticación del usuario que ha iniciado sesión.
     * @return La URL de redirección después de un inicio de sesión exitoso.
     */
    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication) {
        // Verifica el rol del usuario y redirige a la URL correspondiente.
        if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
            return "redirect:/administrador/index";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_CONCURSANTE"))) {
            return "redirect:/concursante/index";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_EVALUADOR"))) {
            return "redirect:/evaluador/index";
        } else {
            // Si el usuario no tiene un rol reconocido, redirige a la página de inicio de
            // sesión.
            return "redirect:/login";
        }
    }

    /**
     * Redirige al catálogo de materiales participantes del concurso actual o del
     * último finalizado.
     *
     * @return Redirección a la edición correspondiente o a la página principal.
     */
    @GetMapping({ "/materiales-participantes", "/materiales-participantes/" })
    public String showMaterialesParticipantesDefault() {
        Concurso concursoActual = concursoService.getConcursoActual();
        if (concursoActual != null && concursoActual.getEdicion() != null) {
            return "redirect:/materiales-participantes/" + concursoActual.getEdicion().replace(" ", "-");
        }
        Concurso ultimoConcurso = concursoService.getUltimoConcurso();
        if (ultimoConcurso != null && ultimoConcurso.getEdicion() != null) {
            return "redirect:/materiales-participantes/" + ultimoConcurso.getEdicion().replace(" ", "-");
        }
        return "redirect:/";
    }

    /**
     * Redirige al listado de materiales ganadores del concurso actual o del último
     * finalizado.
     */
    @GetMapping({ "/materiales-ganadores", "/materiales-ganadores/" })
    public String showMaterialesGanadoresDefault() {
        Concurso concursoActual = concursoService.getConcursoActual();
        if (concursoActual != null && concursoActual.getEdicion() != null) {
            return "redirect:/materiales-ganadores/" + concursoActual.getEdicion().replace(" ", "-");
        }
        Concurso ultimoConcurso = concursoService.getUltimoConcurso();
        if (ultimoConcurso != null && ultimoConcurso.getEdicion() != null) {
            return "redirect:/materiales-ganadores/" + ultimoConcurso.getEdicion().replace(" ", "-");
        }
        return "redirect:/";
    }

    /**
     * Controlador para mostrar la lista de materiales participantes de una edición
     * específica del concurso.
     *
     * @param model         El modelo que se utilizará para pasar datos a la vista.
     * @param edicion       La edición del concurso de la cual se mostrarán los
     *                      materiales participantes.
     * @param redirectAttrs Los atributos de redirección que se utilizarán para
     *                      enviar mensajes al cliente.
     * @return El nombre de la vista que representa la lista de materiales
     *         participantes.
     */
    @Transactional
    @GetMapping("/materiales-participantes/{edicion}")
    public String showMaterialesParticipantes(Model model, @PathVariable String edicion,
            RedirectAttributes redirectAttrs) {

        // Reemplaza los guiones con espacios en la edición.
        edicion = edicion.replace("-", " ");
        // Agrega la edición al modelo para que esté disponible en la vista.
        model.addAttribute("edicion", edicion);

        // Obtiene el concurso asociado a la edición desde el servicio concursoService.
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);

        // Obtiene la lista de materiales participantes del concurso actual desde el
        // servicio materialService.
        List<Material> materialesParticipantes = materialService.getMaterialesParticipantesByConcurso(concurso);
        // Agrega la lista de materiales participantes al modelo para que esté
        // disponible en la vista.
        model.addAttribute("materialesParticipantes", materialesParticipantes);

        // Obtiene la lista de materiales ganadores del concurso actual desde el
        // servicio materialService.
        List<Material> materialesGanadores = materialService.getMaterialesGanadores(concurso);
        // Agrega la lista de materiales ganadores al modelo para que esté disponible en
        // la vista.
        model.addAttribute("materialesGanadores", materialesGanadores);

        // Agrega el mensaje del redirect
        if (redirectAttrs.getFlashAttributes().containsKey("message")) {
            model.addAttribute("message", redirectAttrs.getFlashAttributes().get("message"));
        }

        // Devuelve el nombre de la vista que representa la lista de materiales
        // participantes.
        return "materiales-participantes";
    }

    /**
     * Maneja las solicitudes GET para obtener una vista previa de la primera página
     * del PDF asociado con un material.
     *
     * @param materialId El ID del material del cual se desea obtener la vista
     *                   previa.
     * @return ResponseEntity con el contenido de la vista previa de la primera
     *         página del PDF en formato de imagen PNG.
     * @throws IOException Si ocurre un error durante la obtención o procesamiento
     *                     del material.
     */
    @GetMapping(value = "/materiales-participantes/{materialId}/preview")
    public ResponseEntity<InputStreamResource> getMaterialPreview(@PathVariable Long materialId) throws IOException {
        // Obtiene la ruta del archivo PDF asociado con el materialId
        String pdfFilePath = materialService.getMaterial(materialId).getArchivoPdf();

        // Convierte el archivo PDF en una imagen
        File imageFile = pdfService.convertPdfToImage(pdfFilePath);

        // Crea un FileInputStream para leer el contenido del archivo de imagen
        FileInputStream fis = new FileInputStream(imageFile);

        // Devuelve una ResponseEntity con el contenido de la imagen generada
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(fis));
    }

    /**
     * Controlador para mostrar la página de detalles de un material participante en
     * un concurso.
     *
     * @param model      El modelo que se utilizará para pasar datos a la vista.
     * @param edicion    La edición del concurso a la que pertenece el material.
     * @param materialId El identificador del material cuyos detalles se mostrarán.
     * @return El nombre de la vista que representa la página de detalles del
     *         material participante.
     */
    @Transactional
    @GetMapping("/materiales-participantes/{edicion}/{materialId}")
    public String showMaterialParticipante(Model model, @PathVariable String edicion, @PathVariable Long materialId) {
        // Reemplaza los guiones con espacios en la edición.
        edicion = edicion.replace("-", " ");
        // Agrega la edición al modelo para que esté disponible en la vista.
        model.addAttribute("edicion", edicion);

        // Obtiene el material con el id especificado desde el servicio materialService.
        Material material = materialService.getMaterial(materialId);
        if (material == null) {
            return "redirect:/materiales-participantes/" + edicion.replace(" ", "-");
        }
        // Agrega el material al modelo para que esté disponible en la vista.
        model.addAttribute("material", material);

        // Verifica si la votación está abierta para este material.
        Concurso concurso = material.getConcurso();
        boolean votacionAbierta = false;
        if (concurso != null && concurso.getFechaInicioVotacion() != null && concurso.getFechaFinVotacion() != null) {
            LocalDateTime fechaActual = LocalDateTime.now();
            votacionAbierta = fechaActual.isAfter(concurso.getFechaInicioVotacion())
                    && fechaActual.isBefore(concurso.getFechaFinVotacion());
        }
        model.addAttribute("votacionAbierta", votacionAbierta);

        // Devuelve el nombre de la vista que representa la página de detalles del
        // material participante.
        return "detalle-material";
    }

    /**
     * Controlador para mostrar la lista de materiales ganadores de una edición
     * específica del concurso.
     *
     * @param model   El modelo que se utilizará para pasar datos a la vista.
     * @param edicion La edición del concurso de la cual se mostrarán los materiales
     *                ganadores.
     * @return El nombre de la vista que representa la lista de materiales
     *         ganadores.
     */
    @GetMapping("/materiales-ganadores/{edicion}")
    public String showMaterialesGanadores(Model model, @PathVariable String edicion) {

        // Reemplaza los guiones con espacios en la edición.
        edicion = edicion.replace("-", " ");

        // Obtiene el concurso asociado a la edición desde el servicio concursoService.
        Concurso concurso = concursoService.getConcursoByEdicion(edicion);
        // Obtiene la lista de materiales ganadores del concurso actual desde el
        // servicio materialService.
        List<Material> materialesGanadores = materialService.getMaterialesGanadores(concurso);
        // Agrega la lista de materiales ganadores al modelo para que esté disponible en
        // la vista.
        model.addAttribute("materialesGanadores", materialesGanadores);
        // Agrega la edición al modelo para que esté disponible en la vista.
        model.addAttribute("edicion", edicion);

        // Devuelve el nombre de la vista que representa la lista de materiales
        // ganadores.
        return "materiales-ganadores";
    }

    /**
     * Controlador para dar "like" a un material participante.
     *
     * @param idmaterial    El id del material al que se le dará "like".
     * @param response      La respuesta HTTP que se utilizará para enviar cookies
     *                      al cliente.
     * @param request       La solicitud HTTP que se utilizará para obtener cookies
     *                      del cliente.
     * @param redirectAttrs Los atributos de redirección que se utilizarán para
     *                      enviar mensajes al cliente.
     * @return La URL de redirección después de dar "like" a un material.
     */
    @PostMapping("/materiales-participantes/{idmaterial}/like")
    public String likeMaterial(@PathVariable Long idmaterial, HttpServletResponse response, HttpServletRequest request,
            RedirectAttributes redirectAttrs, Authentication authentication) {
        Material material = materialService.getMaterial(idmaterial);
        if (material == null || material.getConcurso() == null) {
            return "redirect:/";
        }

        String edicion = material.getConcurso().getEdicion().replace(" ", "-");

        // Los usuarios registrados no deben poder votar; solo de forma anónima sin
        // iniciar sesión
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            redirectAttrs.addFlashAttribute("error",
                    "Los usuarios registrados no pueden votar. La votación es exclusiva para el público en general de forma anónima.");
            return "redirect:/materiales-participantes/" + edicion + "/" + idmaterial;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("like_" + idmaterial)) {
                    redirectAttrs.addFlashAttribute("message", "Ya has registrado tu voto para este material.");
                    return "redirect:/materiales-participantes/" + edicion + "/" + idmaterial;
                }
            }
        }

        materialService.darLikeMaterial(material);

        Cookie likeCookie = new Cookie("like_" + idmaterial, "true");
        likeCookie.setMaxAge(60 * 60 * 24 * 365);
        likeCookie.setPath("/");
        likeCookie.setHttpOnly(true);
        response.addCookie(likeCookie);

        redirectAttrs.addFlashAttribute("message",
                "¡Voto registrado para \"" + material.getTitulo() + "\". Muchas gracias por participar!");
        return "redirect:/materiales-participantes/" + edicion + "/" + idmaterial;
    }

    /**
     * Controlador para visualizar el archivo PDF asociado a un material.
     *
     * @param materialId El ID del material cuyo archivo PDF se desea visualizar.
     * @return Una respuesta HTTP que contiene el archivo PDF como recurso.
     */
    @GetMapping("/view/{materialId}")
    public ResponseEntity<Resource> viewPdf(@PathVariable Long materialId) {
        Material material = materialService.getMaterial(materialId);
        if (material == null || material.getArchivoPdf() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(material.getArchivoPdf()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String filename = resource.getFilename() != null ? resource.getFilename()
                        : "material-" + materialId + ".pdf";
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Controlador para descargar directamente el archivo PDF asociado a un
     * material.
     *
     * @param materialId El ID del material cuyo archivo PDF se desea descargar.
     * @return Una respuesta HTTP que descarga el archivo PDF como attachment.
     */
    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long materialId) {
        Material material = materialService.getMaterial(materialId);
        if (material == null || material.getArchivoPdf() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(material.getArchivoPdf()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String filename = resource.getFilename() != null ? resource.getFilename()
                        : "material-" + materialId + ".pdf";
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Controlador para mostrar la página de registro de concursantes.
     *
     * @param model El modelo que se utilizará para pasar datos a la vista.
     * @return El nombre de la vista que representa la página de registro de
     *         concursantes.
     */
    @GetMapping("/participar")
    public String registerConcursante(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "participar";
    }

    /**
     * Controlador para procesar el formulario de registro de concursantes.
     *
     * @param registrationDto    El DTO con los datos del concursante y
     *                           validaciones.
     * @param result             El objeto BindingResult que contendrá los errores
     *                           de validación.
     * @param model              El modelo que se utilizará para pasar datos a la
     *                           vista en caso de error.
     * @param redirectAttributes Atributos de redirección para enviar mensajes de
     *                           éxito.
     * @return La URL de redirección después de procesar el formulario.
     */
    @PostMapping("/participar")
    public String createConcursante(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (registrationDto.getPasswordConfirm() == null
                || !registrationDto.getPassword().equals(registrationDto.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "error.passwordConfirm", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "participar";
        }

        try {
            User user = registrationDto.toEntity();

            userService.createUser(user, User.Rol.CONCURSANTE);
            emailService.sendEmail(user.getEmail(), "Registro exitoso en el Concurso de Materiales Educativos",
                    "¡Gracias por registrarte! Ya puedes postular tus materiales educativos.");

            redirectAttributes.addFlashAttribute("message",
                    "¡Cuenta creada con éxito! Ya puedes iniciar sesión con tus credenciales.");
            return "redirect:/login";
        } catch (UserAlreadyExistsException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "participar";
        }
    }
}
