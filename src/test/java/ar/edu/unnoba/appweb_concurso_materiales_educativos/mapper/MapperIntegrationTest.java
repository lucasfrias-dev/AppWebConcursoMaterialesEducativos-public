package ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.ConcursoDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.EvaluacionDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.MaterialDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.UserRegistrationDto;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapperIntegrationTest {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private ConcursoMapper concursoMapper;

    @Autowired
    private EvaluacionMapper evaluacionMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void testMaterialMapperSpringAndInstance() {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(1L);
        dto.setTitulo("Título Test");
        dto.setDescripcion("Descripción Test");
        dto.setDisciplina("Tecnología");
        dto.setTipoMaterial("Guía");
        dto.setAutores("Autor Test");
        dto.setUrlVideoPresentacion("https://youtube.com/watch?v=test");

        // Test Spring injected mapper
        Material entity = materialMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getTitulo(), entity.getTitulo());
        assertEquals(dto.getDescripcion(), entity.getDescripcion());
        assertEquals(dto.getDisciplina(), entity.getDisciplina());
        assertEquals(dto.getTipoMaterial(), entity.getTipoMaterial());
        assertEquals(dto.getAutores(), entity.getAutores());
        assertEquals(dto.getUrlVideoPresentacion(), entity.getUrlVideoPresentacion());

        MaterialDTO mappedDto = materialMapper.toDto(entity);
        assertNotNull(mappedDto);
        assertEquals(dto.getTitulo(), mappedDto.getTitulo());

        // Test static INSTANCE
        Material instanceEntity = MaterialMapper.INSTANCE.toEntity(dto);
        assertEquals(dto.getTitulo(), instanceEntity.getTitulo());
    }

    @Test
    void testConcursoMapper() {
        ConcursoDTO dto = new ConcursoDTO();
        dto.setId(5L);
        dto.setEdicion("III Edición");
        LocalDateTime now = LocalDateTime.now();
        dto.setFechaInicio(now);
        dto.setFechaFinPostulaciones(now.plusDays(10));
        dto.setFechaInicioVotacion(now.plusDays(11));
        dto.setFechaFinVotacion(now.plusDays(20));
        dto.setFechaFin(now.plusDays(25));

        Concurso entity = concursoMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(5L, entity.getId());
        assertEquals("III Edición", entity.getEdicion());
        assertEquals(dto.getFechaInicio(), entity.getFechaInicio());

        ConcursoDTO mappedDto = concursoMapper.toDto(entity);
        assertNotNull(mappedDto);
        assertEquals("III Edición", mappedDto.getEdicion());
    }

    @Test
    void testEvaluacionMapper() {
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setId(10L);
        dto.setNota(9);
        dto.setComentario("Excelente material pedagógico.");

        Evaluacion entity = evaluacionMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals(9, entity.getNota());
        assertEquals("Excelente material pedagógico.", entity.getComentario());

        EvaluacionDTO mappedDto = evaluacionMapper.toDto(entity);
        assertNotNull(mappedDto);
        assertEquals(9, mappedDto.getNota());
        assertEquals("Excelente material pedagógico.", mappedDto.getComentario());
    }

    @Test
    void testUserMapper() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setNombre("Martín");
        dto.setApellido("Fierro");
        dto.setEmail("martin@unnoba.edu.ar");
        dto.setPassword("SecretPass123");
        dto.setPasswordConfirm("SecretPass123");

        User entity = userMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("Martín", entity.getNombre());
        assertEquals("Fierro", entity.getApellido());
        assertEquals("martin@unnoba.edu.ar", entity.getEmail());
        assertEquals("SecretPass123", entity.getPassword());

        UserRegistrationDto mappedDto = userMapper.toDto(entity);
        assertNotNull(mappedDto);
        assertEquals("Martín", mappedDto.getNombre());
        assertEquals("martin@unnoba.edu.ar", mappedDto.getEmail());
    }
}
