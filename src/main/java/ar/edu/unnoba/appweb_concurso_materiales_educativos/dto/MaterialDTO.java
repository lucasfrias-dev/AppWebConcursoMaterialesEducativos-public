package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper.MaterialMapper;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

@Data
public class MaterialDTO implements Serializable {

    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100, message = "El título no puede tener más de 100 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres")
    private String descripcion;

    @NotBlank(message = "El tipo de material es obligatorio")
    private String tipoMaterial;

    @NotBlank(message = "La disciplina es obligatoria")
    private String disciplina;

    @NotBlank(message = "Los autores son obligatorios")
    private String autores;

    @NotBlank(message = "El link del video de presentación es obligatorio")
    @URL(message = "El link del video de presentación debe ser una URL válida")
    private String urlVideoPresentacion;

    private User concursante;

    public Material toEntity() {
        return MaterialMapper.INSTANCE.toEntity(this);
    }

    public static MaterialDTO fromEntity(Material material) {
        return MaterialMapper.INSTANCE.toDto(material);
    }
}
