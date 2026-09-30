package ar.edu.unnoba.appweb_concurso_materiales_educativos;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.security.SecurityUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asUser(User user) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(new SecurityUserDetails(user));
    }

    @Test
    void testPublicIndexAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testPublicLoginAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testPublicParticiparAccessible() throws Exception {
        mockMvc.perform(get("/participar"))
                .andExpect(status().isOk())
                .andExpect(view().name("participar"));
    }

    @Test
    void testAdminProtectedEndpointRedirectsAnonymous() throws Exception {
        mockMvc.perform(get("/administrador/index"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testConcursanteProtectedEndpointRedirectsAnonymous() throws Exception {
        mockMvc.perform(get("/concursante/index"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testConcursanteCannotAccessAdmin() throws Exception {
        User concursante = new User();
        concursante.setEmail("concursante@example.com");
        concursante.setNombre("Concursante");
        concursante.setApellido("User");
        concursante.setRol(User.Rol.CONCURSANTE);

        mockMvc.perform(get("/administrador/index").with(asUser(concursante)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanAccessAdminIndex() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRol(User.Rol.ADMINISTRADOR);

        mockMvc.perform(get("/administrador/index").with(asUser(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void testConcursanteCanAccessConcursanteIndex() throws Exception {
        User concursante = new User();
        concursante.setEmail("concursante@example.com");
        concursante.setNombre("Concursante");
        concursante.setApellido("User");
        concursante.setRol(User.Rol.CONCURSANTE);

        mockMvc.perform(get("/concursante/index").with(asUser(concursante)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterNewConcursante_Success() throws Exception {
        mockMvc.perform(post("/participar")
                        .with(csrf())
                        .param("nombre", "Laura")
                        .param("apellido", "Gomez")
                        .param("email", "laura.test@example.com")
                        .param("password", "Pass12345")
                        .param("passwordConfirm", "Pass12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testRegisterNewConcursante_PasswordMismatch() throws Exception {
        mockMvc.perform(post("/participar")
                        .with(csrf())
                        .param("nombre", "Pedro")
                        .param("apellido", "Ramirez")
                        .param("email", "pedro@example.com")
                        .param("password", "Pass12345")
                        .param("passwordConfirm", "DifferentPass"))
                .andExpect(model().attributeHasFieldErrors("user", "passwordConfirm"));
    }

    @Test
    void testAdminNewConcursoShowsCalculatedEdicion() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRol(User.Rol.ADMINISTRADOR);

        mockMvc.perform(get("/administrador/new-concurso").with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/new-concurso"))
                .andExpect(model().attributeExists("concurso"))
                .andExpect(model().attributeExists("nextEdicion"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Calculando edición..."))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Edicion")));
    }

    @Test
    void testAdminCreateConcursoValidationFailureReturnsForm() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRol(User.Rol.ADMINISTRADOR);

        mockMvc.perform(post("/administrador/new-concurso")
                        .with(csrf())
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/new-concurso"))
                .andExpect(model().attributeHasErrors("concurso"));
    }

    @Test
    void testConcursanteShowPostularMaterialReturnsFormWithDto() throws Exception {
        User concursante = new User();
        concursante.setEmail("concursante.dto@example.com");
        concursante.setNombre("Concursante");
        concursante.setApellido("User");
        concursante.setRol(User.Rol.CONCURSANTE);

        mockMvc.perform(get("/concursante/postular-material").with(asUser(concursante)))
                .andExpect(status().isOk())
                .andExpect(view().name("concursante/postular-material"))
                .andExpect(model().attributeExists("material"));
    }

    @Test
    void testAdminMaterialesEndpointsAccessible() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRol(User.Rol.ADMINISTRADOR);

        // /administrador/materiales/III-Edicion/all
        mockMvc.perform(get("/administrador/materiales/III-Edicion/all")
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/materiales"))
                .andExpect(model().attributeExists("materiales"))
                .andExpect(model().attributeExists("edicion"));

        // /administrador/materiales/III-Edicion/pendientes-de-evaluacion
        mockMvc.perform(get("/administrador/materiales/III-Edicion/pendientes-de-evaluacion")
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/materiales-pendientes-evaluacion"))
                .andExpect(model().attributeExists("materiales"))
                .andExpect(model().attributeExists("evaluadoresPendientes"))
                .andExpect(model().attributeExists("edicion"));

        // /administrador/materiales/III-Edicion/participantes
        mockMvc.perform(get("/administrador/materiales/III-Edicion/participantes")
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/materiales-participantes"))
                .andExpect(model().attributeExists("materialesParticipantes"))
                .andExpect(model().attributeExists("materialesGanadores"))
                .andExpect(model().attributeExists("edicion"));
    }

    @Test
    void testAdminRegisterEvaluadorAndAdmin() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setRol(User.Rol.ADMINISTRADOR);

        // GET register evaluador
        mockMvc.perform(get("/administrador/register/evaluador")
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/register-evaluador"));

        // POST register evaluador
        mockMvc.perform(post("/administrador/register/evaluador")
                        .with(csrf())
                        .with(asUser(admin))
                        .param("nombre", "Carlos")
                        .param("apellido", "Docente")
                        .param("email", "carlos.evaluador@unnoba.edu.ar")
                        .param("password", "Pass12345")
                        .param("passwordConfirm", "Pass12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrador/evaluadores/registrados"));

        // GET register admin
        mockMvc.perform(get("/administrador/register/admin")
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/register-admin"));

        // POST register admin
        mockMvc.perform(post("/administrador/register/admin")
                        .with(csrf())
                        .with(asUser(admin))
                        .param("nombre", "Sofia")
                        .param("apellido", "Admin")
                        .param("email", "sofia.admin@unnoba.edu.ar")
                        .param("password", "Pass12345")
                        .param("passwordConfirm", "Pass12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrador/admin/registrados"));
    }

    @Test
    void testMaterialesParticipantesDefaultRedirects() throws Exception {
        mockMvc.perform(get("/materiales-participantes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testMaterialesGanadoresDefaultRedirects() throws Exception {
        mockMvc.perform(get("/materiales-ganadores"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testDownloadAndNonExistentMaterialReturns404Cleanly() throws Exception {
        mockMvc.perform(get("/download/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testViewNonExistentMaterialReturns404Cleanly() throws Exception {
        mockMvc.perform(get("/view/999999"))
                .andExpect(status().isNotFound());
    }

    @Autowired(required = false)
    private ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.MaterialRepository materialRepository;

    @Autowired(required = false)
    private ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.ConcursoRepository concursoRepository;

    @Autowired(required = false)
    private ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.UserRepository userRepository;

    @Test
    void testAdminShowMaterialDetailViewNonExistentRedirects() throws Exception {
        User admin = new User();
        admin.setEmail("admin.test@example.com");
        admin.setNombre("Admin");
        admin.setApellido("Tester");
        admin.setRol(User.Rol.ADMINISTRADOR);

        mockMvc.perform(get("/administrador/materiales/III-Edicion/999999")
                        .with(asUser(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrador/materiales/III-Edicion/all"));
    }

    @Test
    void testAdminShowMaterialDetailViewExistingReturnsOk() throws Exception {
        User admin = new User();
        admin.setEmail("admin.tester2@example.com");
        admin.setNombre("Admin2");
        admin.setApellido("Tester2");
        admin.setPassword("Pass12345");
        admin.setRol(User.Rol.ADMINISTRADOR);
        if (userRepository != null) {
            admin = userRepository.save(admin);
        }

        ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso concurso = new ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso();
        concurso.setEdicion("III Edición");
        concurso.setFechaInicio(java.time.LocalDateTime.now().minusDays(10));
        concurso.setFechaFin(java.time.LocalDateTime.now().plusDays(10));
        concurso.setFechaFinPostulaciones(java.time.LocalDateTime.now().minusDays(5));
        concurso.setFechaInicioVotacion(java.time.LocalDateTime.now().minusDays(4));
        concurso.setFechaFinVotacion(java.time.LocalDateTime.now().plusDays(5));
        if (concursoRepository != null) {
            concurso = concursoRepository.save(concurso);
        }

        ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material mat = new ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material();
        mat.setTitulo("Material de Prueba");
        mat.setDescripcion("Descripción de prueba");
        mat.setTipoMaterial("Video");
        mat.setDisciplina("Tecnología");
        mat.setAutores("Autor de Prueba");
        mat.setUrlVideoPresentacion("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        mat.setConcurso(concurso);
        mat.setConcursante(admin);
        mat.setLikes(0);
        if (materialRepository != null) {
            mat = materialRepository.save(mat);
        }

        mockMvc.perform(get("/administrador/materiales/III-Edicion/" + mat.getId())
                        .with(asUser(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("administrador/detalle-material"));
    }

    @Test
    void testAdminAsignarMaterialRedirectsWithFlashAttributes() throws Exception {
        User admin = new User();
        admin.setEmail("admin.assign@example.com");
        admin.setNombre("Admin");
        admin.setApellido("Assign");
        admin.setRol(User.Rol.ADMINISTRADOR);

        mockMvc.perform(post("/administrador/evaluadores/asignar-material")
                        .with(csrf())
                        .with(asUser(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrador/evaluadores/asignar-material"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testEvaluadorMaterialEvaluarEndpointAccessible() throws Exception {
        User evaluador = new User();
        evaluador.setEmail("evaluador.test@example.com");
        evaluador.setNombre("Carlos");
        evaluador.setApellido("Evaluador");
        evaluador.setRol(User.Rol.EVALUADOR);

        mockMvc.perform(get("/evaluador/materiales/999999/evaluar")
                        .with(asUser(evaluador)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/evaluador/materiales/asignados"));
    }

    @Test
    void testAuthenticatedUserCannotVote() throws Exception {
        User user = new User();
        user.setEmail("user.voter@example.com");
        user.setNombre("Voter");
        user.setApellido("Registered");
        user.setRol(User.Rol.CONCURSANTE);

        mockMvc.perform(post("/materiales-participantes/1/like")
                        .with(csrf())
                        .with(asUser(user)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }
}

