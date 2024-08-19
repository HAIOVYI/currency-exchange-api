package projects.currencyexchangeapi.dto.rate;

import java.math.BigDecimal;

public record CurrencyRateResponseDto(

        BigDecimal rate
) {
}
