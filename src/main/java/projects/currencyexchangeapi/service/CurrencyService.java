package projects.currencyexchangeapi.service;

import java.util.List;
import projects.currencyexchangeapi.dto.currency.CreateCurrencyRequestDto;
import projects.currencyexchangeapi.dto.currency.CurrencyResponseDto;

public interface CurrencyService {

    CurrencyResponseDto getById(Long id);

    List<CurrencyResponseDto> getAll();

    CurrencyResponseDto create(CreateCurrencyRequestDto requestDto);

    void delete(Long currencyId);
}
