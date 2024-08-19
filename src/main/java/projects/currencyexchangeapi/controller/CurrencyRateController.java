package projects.currencyexchangeapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import projects.currencyexchangeapi.dto.rate.CreateCurrencyRateRequestDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDetailDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRetrospectiveResponseDto;
import projects.currencyexchangeapi.service.CurrencyRateService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Currency rate management", description = "Endpoints for managing currency rates")
@RequestMapping("/currency-rate")
public class CurrencyRateController {

    private final CurrencyRateService currencyRateService;

    @Operation(summary = "Get current exchange rate",
            description = "Retrieve the current exchange rate between two currencies.")
    @GetMapping("/current")
    public CurrencyRateResponseDto getCurrentRate(
            @RequestParam(defaultValue = "UAH")
            String fromCurrency,
            @RequestParam(defaultValue = "USD")
            String toCurrency) {

        return currencyRateService.getCurrentRate(fromCurrency, toCurrency);
    }

    @Operation(summary = "Get historical exchange rates",
            description = "Retrieve historical exchange rates between two currencies for a specified date range.")
    @GetMapping("/history")
    public CurrencyRetrospectiveResponseDto getRateHistory(
            @RequestParam(defaultValue = "UAH")
            String fromCurrency,
            @RequestParam(defaultValue = "USD")
            String toCurrency,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().minusWeeks(1)}")
            LocalDate fromDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
            LocalDate toDate) {

        return currencyRateService.getRate(fromCurrency, toCurrency, fromDate, toDate);
    }

    @Operation(summary = "Get base currency rates",
            description = "Retrieve a list of exchange rates for a base currency against other currencies.")
    @GetMapping("/base")
    public List<CurrencyRateResponseDetailDto> getBaseRates(@RequestParam(defaultValue = "USD")
                                                            String baseCurrency) {
        return currencyRateService.getAllBaseRates(baseCurrency);
    }

    @Operation(summary = "Create a new currency rate",
            description = "Create a new currency rate entry. This endpoint requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyRateResponseDetailDto create(@Valid @RequestBody CreateCurrencyRateRequestDto requestDto) {
        return currencyRateService.create(requestDto);
    }
}
