package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper.UserMapper;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String apellido;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número")
    private String password;

    private String passwordConfirm;

    public User toEntity() {
        return UserMapper.INSTANCE.toEntity(this);
    }

    public static UserRegistrationDto fromEntity(User user) {
        return UserMapper.INSTANCE.toDto(user);
    }
}
