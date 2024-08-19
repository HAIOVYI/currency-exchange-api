package projects.currencyexchangeapi.mapper;

import org.mapstruct.Mapper;
import projects.currencyexchangeapi.config.MapperConfig;
import projects.currencyexchangeapi.dto.registration.UserRegisterRequestDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterResponseDto;
import projects.currencyexchangeapi.entity.UserEntity;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    UserEntity toUserEntity(UserRegisterRequestDto requestDto);

    UserRegisterResponseDto toResponseDto(UserEntity userEntity);
}
