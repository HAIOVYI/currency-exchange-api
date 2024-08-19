package projects.currencyexchangeapi.service;

import java.time.LocalDate;
import java.util.List;
import projects.currencyexchangeapi.dto.rate.CreateCurrencyRateRequestDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDetailDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRetrospectiveResponseDto;

public interface CurrencyRateService {

    CurrencyRateResponseDto getCurrentRate(String fromCurrency, String toCurrency);

    CurrencyRetrospectiveResponseDto getRate(String fromCurrency,
                                             String toCurrency,
                                             LocalDate fromDate,
                                             LocalDate toDate);

    List<CurrencyRateResponseDetailDto> getAllBaseRates(String baseCurrency);

    CurrencyRateResponseDetailDto create(CreateCurrencyRateRequestDto requestDto);
}
