package ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.MaterialDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialMapper {

    MaterialMapper INSTANCE = Mappers.getMapper(MaterialMapper.class);

    Material toEntity(MaterialDTO dto);

    MaterialDTO toDto(Material entity);
}
