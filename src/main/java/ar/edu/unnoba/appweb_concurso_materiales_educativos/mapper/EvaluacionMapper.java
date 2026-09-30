package ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.EvaluacionDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EvaluacionMapper {

    EvaluacionMapper INSTANCE = Mappers.getMapper(EvaluacionMapper.class);

    Evaluacion toEntity(EvaluacionDTO dto);

    EvaluacionDTO toDto(Evaluacion entity);
}
