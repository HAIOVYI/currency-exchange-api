package projects.currencyexchangeapi.service.impl;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import projects.currencyexchangeapi.dto.exchange.ConfirmExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.CurrencyEntity;
import projects.currencyexchangeapi.entity.ExchangeEntity;
import projects.currencyexchangeapi.entity.UserEntity;
import projects.currencyexchangeapi.exception.CurrencyNotFoundException;
import projects.currencyexchangeapi.exception.ExchangeNotFoundException;
import projects.currencyexchangeapi.holder.PendingExchangeHolder;
import projects.currencyexchangeapi.mapper.ExchangeMapper;
import projects.currencyexchangeapi.repository.CurrencyRepository;
import projects.currencyexchangeapi.repository.ExchangeRepository;
import projects.currencyexchangeapi.service.CurrencyRateService;
import projects.currencyexchangeapi.service.ExchangeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {

    private final ExchangeMapper exchangeMapper;
    private final ExchangeRepository exchangeRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyRateService currencyRateService;
    private final PendingExchangeHolder pendingExchangeHolder;

    @Override
    public ExchangeResponseDto create(ExchangeRequestDto requestDto, UserEntity user) {
        log.info("Creating exchange request: {}", requestDto);

        ExchangeEntity exchange = prepareExchange(requestDto, user);
        exchangeRepository.save(exchange);

        if (calculateAvailableBalanceWithReserve(exchange)
                .compareTo(calculateRequiredAmount(exchange)) >= 0) {
            updateBalances(exchange, calculateRequiredAmount(exchange));

            log.info("Exchange completed successfully: {}", exchange.getId());
        } else {
            handleInsufficientFunds(exchange);

            log.warn("Insufficient funds for exchange: {}", exchange.getId());
        }
        return exchangeMapper.toDto(exchange);
    }

    @Override
    public ExchangeResponseDto confirm(ConfirmExchangeRequestDto requestDto, UserEntity user) {
        log.info("Confirming exchange request: {}", requestDto);

        ExchangeEntity exchange = getExchangeById(requestDto.exchangeId());

        if (!exchange.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    String.format("Exchange %d does not belong to user: %d", exchange.getId(), user.getId()));
        }

        if (requestDto.confirmed() && exchange.getRequestStatus() == ExchangeEntity.RequestStatus.PENDING) {
            if (calculateAvailableBalanceWithoutReserve(exchange)
                    .compareTo(calculateRequiredAmount(exchange)) >= 0) {
                updateBalances(exchange, calculateRequiredAmount(exchange));

                cancelCurrencyReserve(exchange);
                pendingExchangeHolder.getPendingRequestTimers().remove(exchange);
                log.info("Exchange confirmed and completed: {}", exchange.getId());
            } else {
                handleInsufficientFunds(exchange);
                log.warn("Insufficient funds for exchange confirmation: {}", exchange.getId());
            }
        } else {
            log.info("Exchange with id: {} already cancelled", exchange.getId());
        }

        return exchangeMapper.toDto(exchange);
    }

    @Override
    public BigDecimal getActualRate(ExchangeEntity exchange) {
        log.info("Update the exchange rate for exchange request, exchange id: {}", exchange.getId());
        return currencyRateService.getCurrentRate(
                exchange.getCurrencyFrom().getCode(),
                exchange.getCurrencyTo().getCode()).rate();
    }

    @Override
    public void cancelCurrencyReserve(ExchangeEntity exchange) {
        pendingExchangeHolder.getCurrencyReserveMap().computeIfPresent(exchange.getCurrencyTo().getId(),
                (key, value) -> value.subtract(calculateRequiredAmount(exchange)));

        log.info("Cancelled currency reserve for exchange: {}", exchange.getId());
    }

    @Override
    public BigDecimal calculateRequiredAmount(ExchangeEntity exchange) {
        log.info("Calculate required amount for exchange, exchange id: {}", exchange.getId());
        return exchange.getAmount().multiply(exchange.getRate());
    }

    @Override
    public BigDecimal calculateAvailableBalanceWithReserve(ExchangeEntity exchange) {
        log.info("Calculate available balance with reserve for currency, currency id: {}",
                exchange.getCurrencyTo().getId());

        return findCurrencyById(exchange.getCurrencyTo().getId()).getBalance()
                .subtract(pendingExchangeHolder.getCurrencyReserveMap().getOrDefault(
                        exchange.getCurrencyTo().getId(), BigDecimal.ZERO));
    }

    private BigDecimal calculateAvailableBalanceWithoutReserve(ExchangeEntity exchange) {
        log.info("Calculate available balance for currency, currency id: {}",
                exchange.getCurrencyTo().getId());

        return findCurrencyById(exchange.getCurrencyTo().getId()).getBalance();
    }

    private ExchangeEntity prepareExchange(ExchangeRequestDto requestDto, UserEntity user) {
        ExchangeEntity exchangeEntity = exchangeMapper.toEntity(requestDto);
        exchangeEntity.setUser(user);

        exchangeEntity.setCurrencyFrom(findCurrencyById(requestDto.currencyFromId()));
        exchangeEntity.setCurrencyTo(findCurrencyById(requestDto.currencyToId()));

        exchangeEntity.setRate(getActualRate(exchangeEntity));

        return exchangeEntity;
    }

    private ExchangeEntity getExchangeById(Long id) {
        return exchangeRepository.findById(id).orElseThrow(
                () -> new ExchangeNotFoundException(String.format("Exchange with id: %d not found in DB", id)));
    }

    private CurrencyEntity findCurrencyById(Long id) {
        return currencyRepository.findById(id).orElseThrow(() ->
                new CurrencyNotFoundException(String.format("Currency with id: %d not found", id)));
    }

    private void updateBalances(ExchangeEntity exchange, BigDecimal requiredAmount) {
        CurrencyEntity toCurrency = exchange.getCurrencyTo();
        toCurrency.setBalance(toCurrency.getBalance().subtract(requiredAmount));
        currencyRepository.save(toCurrency);

        CurrencyEntity fromCurrency = exchange.getCurrencyFrom();
        fromCurrency.setBalance(fromCurrency.getBalance().add(exchange.getAmount()));
        currencyRepository.save(fromCurrency);

        exchange.setRequestStatus(ExchangeEntity.RequestStatus.COMPLETED);
        exchangeRepository.save(exchange);
    }

    private void handleInsufficientFunds(ExchangeEntity exchange) {
        log.info("Starting check if exchange not already in queue, exchange id: {}", exchange.getId());
        if (!pendingExchangeHolder.getPendingRequests().contains(exchange)) {
            pendingExchangeHolder.getPendingRequests().offer(exchange);

            cancelCurrencyReserve(exchange);
            pendingExchangeHolder.getPendingRequestTimers().remove(exchange);
            log.info("Add exchange request to the queue, exchange id: {}", exchange.getId());
        } else {
            log.info("Exchange already in queue, exchange id: {}", exchange.getId());
        }
    }
}
