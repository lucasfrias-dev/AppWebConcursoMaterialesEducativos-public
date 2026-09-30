package ar.edu.unnoba.appweb_concurso_materiales_educativos.dto;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper.ConcursoMapper;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ConcursoDTO implements Serializable {

    private Long id;

    private String edicion;

    @NotNull(message = "La fecha y hora de inicio de la convocatoria es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicio;

    @NotNull(message = "El cierre del período de postulación de materiales es obligatorio")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFinPostulaciones;

    @NotNull(message = "El inicio de votación del público es obligatorio")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicioVotacion;

    @NotNull(message = "El cierre de votación del público es obligatorio")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFinVotacion;

    @NotNull(message = "La fecha y hora de finalización del concurso es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFin;

    public Concurso toEntity() {
        return ConcursoMapper.INSTANCE.toEntity(this);
    }

    public static ConcursoDTO fromEntity(Concurso concurso) {
        return ConcursoMapper.INSTANCE.toDto(concurso);
    }
}
