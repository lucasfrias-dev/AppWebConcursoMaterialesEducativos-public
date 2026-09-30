package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConcursoDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ConcursoDTO createValidDto() {
        LocalDateTime now = LocalDateTime.now();
        ConcursoDTO dto = new ConcursoDTO();
        dto.setEdicion("IV Edición");
        dto.setFechaInicio(now);
        dto.setFechaFinPostulaciones(now.plusDays(10));
        dto.setFechaInicioVotacion(now.plusDays(11));
        dto.setFechaFinVotacion(now.plusDays(20));
        dto.setFechaFin(now.plusDays(25));
        return dto;
    }

    @Test
    void testValidConcursoDtoHasNoViolations() {
        ConcursoDTO dto = createValidDto();
        Set<ConstraintViolation<ConcursoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un ConcursoDTO con fechas válidas no debería tener violaciones");
    }

    @Test
    void testNullFechaInicioFailsValidation() {
        ConcursoDTO dto = createValidDto();
        dto.setFechaInicio(null);
        Set<ConstraintViolation<ConcursoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fechaInicio")));
    }

    @Test
    void testNullFechaFinFailsValidation() {
        ConcursoDTO dto = createValidDto();
        dto.setFechaFin(null);
        Set<ConstraintViolation<ConcursoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fechaFin")));
    }

    @Test
    void testToEntityAndFromEntityMapping() {
        ConcursoDTO dto = createValidDto();
        dto.setId(5L);

        Concurso entity = dto.toEntity();
        assertEquals(5L, entity.getId());
        assertEquals("IV Edición", entity.getEdicion());
        assertEquals(dto.getFechaInicio(), entity.getFechaInicio());

        ConcursoDTO roundTrip = ConcursoDTO.fromEntity(entity);
        assertEquals(dto.getId(), roundTrip.getId());
        assertEquals(dto.getEdicion(), roundTrip.getEdicion());
        assertEquals(dto.getFechaFinPostulaciones(), roundTrip.getFechaFinPostulaciones());
    }
}
