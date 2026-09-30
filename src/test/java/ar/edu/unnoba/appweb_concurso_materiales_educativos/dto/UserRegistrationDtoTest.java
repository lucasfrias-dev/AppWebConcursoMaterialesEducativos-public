package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UserRegistrationDto createValidDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setNombre("Carlos");
        dto.setApellido("Perez");
        dto.setEmail("carlos.perez@example.com");
        dto.setPassword("Secret123");
        dto.setPasswordConfirm("Secret123");
        return dto;
    }

    @Test
    void testValidDtoHasNoViolations() {
        UserRegistrationDto dto = createValidDto();
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un DTO con datos válidos no debería tener violaciones");
    }

    @Test
    void testBlankNombreFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setNombre("   ");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    void testBlankApellidoFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setApellido("");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apellido")));
    }

    @Test
    void testInvalidEmailFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setEmail("correo-invalido");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testBlankEmailFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setEmail("");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testPasswordShortLengthFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setPassword("Ab1"); // Menos de 8 caracteres
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testPasswordWithoutUppercaseFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setPassword("secret123"); // Sin mayúsculas
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testPasswordWithoutNumberFailsValidation() {
        UserRegistrationDto dto = createValidDto();
        dto.setPassword("SecretWord"); // Sin números
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
