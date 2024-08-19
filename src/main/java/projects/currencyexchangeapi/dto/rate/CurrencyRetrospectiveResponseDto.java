package projects.currencyexchangeapi.dto.rate;

import java.math.BigDecimal;

public record CurrencyRetrospectiveResponseDto(

        BigDecimal minRate,

        BigDecimal maxRate
) {
}
