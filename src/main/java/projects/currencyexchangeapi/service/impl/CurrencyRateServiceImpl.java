package projects.currencyexchangeapi.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import projects.currencyexchangeapi.dto.rate.CreateCurrencyRateRequestDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDetailDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRetrospectiveResponseDto;
import projects.currencyexchangeapi.entity.CurrencyEntity;
import projects.currencyexchangeapi.entity.CurrencyRateEntity;
import projects.currencyexchangeapi.entity.CurrencyRateProjection;
import projects.currencyexchangeapi.exception.CurrencyNotFoundException;
import projects.currencyexchangeapi.exception.InvalidDateException;
import projects.currencyexchangeapi.mapper.CurrencyRateMapper;
import projects.currencyexchangeapi.repository.CurrencyRateRepository;
import projects.currencyexchangeapi.repository.CurrencyRepository;
import projects.currencyexchangeapi.service.CurrencyRateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyRateServiceImpl implements CurrencyRateService {

    private static final int DEFAULT_RATE = 1;

    private final CurrencyRateRepository currencyRateRepository;
    private final CurrencyRateMapper currencyRateMapper;
    private final CurrencyRepository currencyRepository;

    @Override
    public CurrencyRateResponseDto getCurrentRate(String fromCurrency, String toCurrency) {
        log.info("Fetching current rate, from currency: {}, to currency: {}",
                fromCurrency, toCurrency);

        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            log.info("Currencies are the same, returning default rate");

            return new CurrencyRateResponseDto(new BigDecimal(DEFAULT_RATE));
        }

        CurrencyRateResponseDto rate = new CurrencyRateResponseDto(
                currencyRateRepository.getCurrentRate(fromCurrency, toCurrency));

        log.info("Fetched current rate, from currency: {}, to currency: {}, rate: {}",
                fromCurrency, toCurrency, rate);

        return rate;
    }

    @Override
    public CurrencyRetrospectiveResponseDto getRate(String fromCurrency,
                                                    String toCurrency,
                                                    LocalDate fromDate,
                                                    LocalDate toDate) {
        log.info("Fetching rate retrospectively, from currency: {}, to currency: {}, from date: {}, to date: {}",
                fromCurrency, toCurrency, fromDate, toDate);

        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);
        validateDates(fromDate, toDate);

        if (fromCurrency.equals(toCurrency)) {
            log.info("Currencies are the same, returning default rate for both min and max");

            return new CurrencyRetrospectiveResponseDto(
                    new BigDecimal(DEFAULT_RATE), new BigDecimal(DEFAULT_RATE));
        }

        CurrencyRateProjection rateProjection = currencyRateRepository
                .getRate(fromCurrency, toCurrency, fromDate, toDate);

        log.info("Fetched rate retrospectively, min rate: {}, max rate: {}",
                rateProjection.getMinRate(), rateProjection.getMaxRate());

        return new CurrencyRetrospectiveResponseDto(rateProjection.getMinRate(), rateProjection.getMaxRate());
    }

    @Override
    public List<CurrencyRateResponseDetailDto> getAllBaseRates(String baseCurrency) {
        log.info("Fetching all base rates for currency: {}", baseCurrency);

        validateCurrency(baseCurrency);

        List<CurrencyRateResponseDetailDto> rates = currencyRateRepository.findAllByBaseCurrency(baseCurrency)
                .stream()
                .map(currencyRateMapper::toDto)
                .toList();

        log.info("Fetched all base rates, base currency: {}, total count: {}", baseCurrency, rates.size());

        return rates;
    }

    @Override
    public CurrencyRateResponseDetailDto create(CreateCurrencyRateRequestDto requestDto) {
        log.info("Creating currency rate, currency id: {}, rate: {}",
                requestDto.currencyId(), requestDto.rate());

        CurrencyEntity currencyEntity = validateCurrency(requestDto.currencyId());
        CurrencyRateEntity rateEntity = currencyRateMapper.toEntity(requestDto);

        rateEntity.setCurrency(currencyEntity);
        rateEntity.setRate(requestDto.rate());

        CurrencyRateResponseDetailDto response = currencyRateMapper.toDto(currencyRateRepository.save(rateEntity));

        log.info("Currency rate created successfully, currency id: {}, rate: {}",
                requestDto.currencyId(), requestDto.rate());

        return response;
    }

    private CurrencyEntity validateCurrency(Long currencyId) {
        log.info("Validating currency, currency id: {}", currencyId);

        return currencyRepository.findById(currencyId).orElseThrow(() ->
                new CurrencyNotFoundException("Currency with id " + currencyId + " not found"));
    }

    private void validateCurrency(String currencyCode) {
        log.info("Validating currency, currency code: {}", currencyCode);

        if (!currencyRepository.existsByCode(currencyCode)) {
            log.warn("Validation failed, currency with code {} not found", currencyCode);

            throw new CurrencyNotFoundException("Currency with code " + currencyCode + " not found");
        }
        log.info("Validation successful, currency code: {}", currencyCode);
    }

    private void validateDates(LocalDate fromDate, LocalDate toDate) {
        log.info("Validating dates, from date: {}, to date: {}", fromDate, toDate);

        if (fromDate.isAfter(toDate)) {
            log.warn("Validation failed, 'fromDate' {} is after 'toDate' {}", fromDate, toDate);

            throw new InvalidDateException("The 'fromDate' cannot be after the 'toDate'");
        }

        if (fromDate.isAfter(LocalDate.now()) || toDate.isAfter(LocalDate.now())) {
            log.warn("Validation failed, dates cannot be in the future, from date: {}, to date: {}", fromDate, toDate);

            throw new InvalidDateException("Dates cannot be in the future");
        }

        log.info("Validation successful, from date: {}, to date: {}", fromDate, toDate);
    }
}
