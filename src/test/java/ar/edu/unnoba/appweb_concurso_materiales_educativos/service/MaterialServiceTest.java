package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.ConcursoRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.EvaluacionRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @InjectMocks
    private MaterialServiceImpl materialService;

    private Material material;
    private User user;
    private Concurso concurso;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("autor@example.com");

        concurso = new Concurso();
        concurso.setId(1L);
        concurso.setEdicion("I");

        material = new Material();
        material.setId(10L);
        material.setTitulo("Material Didáctico 1");
        material.setDescripcion("Una descripción educativa adecuada");
        material.setTipoMaterial("Software");
        material.setDisciplina("Matemática");
        material.setAutores("Profesor X");
        material.setLikes(0);
    }

    @Test
    void testCreateMaterial() {
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial(material, user, concurso);

        assertNotNull(result);
        assertEquals(user, result.getConcursante());
        assertEquals(concurso, result.getConcurso());
        verify(materialRepository, times(1)).save(material);
    }

    @Test
    void testDarLike() {
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        materialService.darLikeMaterial(material);

        assertEquals(1, material.getLikes());
        verify(materialRepository, times(1)).save(material);
    }

    @Test
    void testFindById() {
        when(materialRepository.findMaterialById(10L)).thenReturn(material);

        Material found = materialService.getMaterial(10L);

        assertNotNull(found);
        assertEquals("Material Didáctico 1", found.getTitulo());
    }
}
