package ar.edu.unnoba.appweb_concurso_materiales_educativos.mapper;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.UserRegistrationDto;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserRegistrationDto dto);

    UserRegistrationDto toDto(User entity);
}
