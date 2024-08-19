package projects.currencyexchangeapi.mapper;

import org.mapstruct.Mapper;
import projects.currencyexchangeapi.config.MapperConfig;
import projects.currencyexchangeapi.dto.currency.CreateCurrencyRequestDto;
import projects.currencyexchangeapi.dto.currency.CurrencyResponseDto;
import projects.currencyexchangeapi.entity.CurrencyEntity;

@Mapper(config = MapperConfig.class)
public interface CurrencyMapper {

    CurrencyResponseDto toDto(CurrencyEntity currency);

    CurrencyEntity toEntity(CreateCurrencyRequestDto requestDto);
}
