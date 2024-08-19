package projects.currencyexchangeapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import projects.currencyexchangeapi.config.MapperConfig;
import projects.currencyexchangeapi.dto.rate.CreateCurrencyRateRequestDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDetailDto;
import projects.currencyexchangeapi.entity.CurrencyRateEntity;

@Mapper(config = MapperConfig.class, uses = CurrencyMapper.class)
public interface CurrencyRateMapper {

    @Mapping(source = "currency.id", target = "currencyId")
    @Mapping(source = "currency.code", target = "currencyCode")
    @Mapping(source = "currency.name", target = "currencyName")
    CurrencyRateResponseDetailDto toDto(CurrencyRateEntity currencyRate);

    @Mapping(source = "currencyId", target = "currency.id")
    CurrencyRateEntity toEntity(CreateCurrencyRateRequestDto requestDto);
}
