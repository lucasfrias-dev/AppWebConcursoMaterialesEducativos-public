package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EvaluacionDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EvaluacionDTO createValidDto() {
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setNota(8);
        dto.setComentario("Excelente calidad pedagógica y claridad expositiva.");
        return dto;
    }

    @Test
    void testValidEvaluacionDtoHasNoViolations() {
        EvaluacionDTO dto = createValidDto();
        Set<ConstraintViolation<EvaluacionDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un EvaluacionDTO con datos válidos no debería tener violaciones");
    }

    @Test
    void testNotaOutOfRangeFailsValidation() {
        EvaluacionDTO dto = createValidDto();
        dto.setNota(12); // Max es 10
        Set<ConstraintViolation<EvaluacionDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nota")));

        dto.setNota(-1); // Min es 0
        violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nota")));
    }

    @Test
    void testBlankComentarioFailsValidation() {
        EvaluacionDTO dto = createValidDto();
        dto.setComentario("   ");
        Set<ConstraintViolation<EvaluacionDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("comentario")));
    }

    @Test
    void testToEntityAndFromEntityMapping() {
        EvaluacionDTO dto = createValidDto();
        dto.setId(12L);

        Evaluacion entity = dto.toEntity();
        assertEquals(12L, entity.getId());
        assertEquals(8, entity.getNota());
        assertEquals(dto.getComentario(), entity.getComentario());

        EvaluacionDTO roundTrip = EvaluacionDTO.fromEntity(entity);
        assertEquals(dto.getId(), roundTrip.getId());
        assertEquals(dto.getNota(), roundTrip.getNota());
        assertEquals(dto.getComentario(), roundTrip.getComentario());
    }
}
