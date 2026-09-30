package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper.EvaluacionMapper;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class EvaluacionDTO implements Serializable {

    private Long id;

    @NotNull(message = "La calificación numérica es obligatoria")
    @Min(value = 0, message = "La calificación no puede ser menor a 0")
    @Max(value = 10, message = "La calificación no puede ser mayor a 10")
    private Integer nota;

    @NotBlank(message = "El dictamen u observaciones pedagógicas son obligatorias")
    private String comentario;

    public Evaluacion toEntity() {
        return EvaluacionMapper.INSTANCE.toEntity(this);
    }

    public static EvaluacionDTO fromEntity(Evaluacion evaluacion) {
        return EvaluacionMapper.INSTANCE.toDto(evaluacion);
    }
}
