package projects.currencyexchangeapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import projects.currencyexchangeapi.config.MapperConfig;
import projects.currencyexchangeapi.dto.exchange.ExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.ExchangeEntity;

@Mapper(config = MapperConfig.class, uses = {CurrencyMapper.class, UserMapper.class})
public interface ExchangeMapper {

    @Mapping(source = "currencyFromId", target = "currencyFrom.id")
    @Mapping(source = "currencyToId", target = "currencyTo.id")
    ExchangeEntity toEntity(ExchangeRequestDto requestDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "currencyFrom.id", target = "currencyFromId")
    @Mapping(source = "currencyTo.id", target = "currencyToId")
    ExchangeResponseDto toDto(ExchangeEntity entity);
}
