package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MaterialDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private MaterialDTO createValidDto() {
        MaterialDTO dto = new MaterialDTO();
        dto.setTitulo("Guía Didáctica de POO");
        dto.setDescripcion("Una guía introductoria a la programación orientada a objetos.");
        dto.setTipoMaterial("Guía");
        dto.setDisciplina("Informática");
        dto.setAutores("Equipo Docente UNNOBA");
        dto.setUrlVideoPresentacion("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        return dto;
    }

    @Test
    void testValidMaterialDtoHasNoViolations() {
        MaterialDTO dto = createValidDto();
        Set<ConstraintViolation<MaterialDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un DTO con datos válidos no debería tener violaciones");
    }

    @Test
    void testBlankTituloFailsValidation() {
        MaterialDTO dto = createValidDto();
        dto.setTitulo("  ");
        Set<ConstraintViolation<MaterialDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void testBlankDescripcionFailsValidation() {
        MaterialDTO dto = createValidDto();
        dto.setDescripcion("");
        Set<ConstraintViolation<MaterialDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descripcion")));
    }

    @Test
    void testInvalidVideoUrlFailsValidation() {
        MaterialDTO dto = createValidDto();
        dto.setUrlVideoPresentacion("no-es-una-url-valida");
        Set<ConstraintViolation<MaterialDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("urlVideoPresentacion")));
    }

    @Test
    void testToEntityAndFromEntityMapping() {
        MaterialDTO dto = createValidDto();
        dto.setId(100L);

        Material entity = dto.toEntity();
        assertEquals(100L, entity.getId());
        assertEquals("Guía Didáctica de POO", entity.getTitulo());
        assertEquals("Informática", entity.getDisciplina());

        MaterialDTO roundTrip = MaterialDTO.fromEntity(entity);
        assertEquals(dto.getId(), roundTrip.getId());
        assertEquals(dto.getTitulo(), roundTrip.getTitulo());
        assertEquals(dto.getDescripcion(), roundTrip.getDescripcion());
    }
}
