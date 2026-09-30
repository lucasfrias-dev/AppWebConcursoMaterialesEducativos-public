package ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.ConcursoDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConcursoMapper {

    ConcursoMapper INSTANCE = Mappers.getMapper(ConcursoMapper.class);

    Concurso toEntity(ConcursoDTO dto);

    ConcursoDTO toDto(Concurso entity);
}
