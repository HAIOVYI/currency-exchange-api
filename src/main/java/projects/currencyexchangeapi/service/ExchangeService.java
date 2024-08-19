package projects.currencyexchangeapi.service;

import java.math.BigDecimal;
import projects.currencyexchangeapi.dto.exchange.ConfirmExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.ExchangeEntity;
import projects.currencyexchangeapi.entity.UserEntity;

public interface ExchangeService {

    ExchangeResponseDto create(ExchangeRequestDto requestDto, UserEntity user);

    ExchangeResponseDto confirm(ConfirmExchangeRequestDto requestDto, UserEntity user);

    BigDecimal getActualRate(ExchangeEntity exchange);

    BigDecimal calculateAvailableBalanceWithReserve(ExchangeEntity exchange);

    BigDecimal calculateRequiredAmount(ExchangeEntity exchange);

    void cancelCurrencyReserve(ExchangeEntity exchange);
}
