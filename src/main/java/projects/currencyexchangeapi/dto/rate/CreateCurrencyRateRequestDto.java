package projects.currencyexchangeapi.dto.rate;

import java.math.BigDecimal;

public record CreateCurrencyRateRequestDto(

        Long currencyId,

        BigDecimal rate
) {
}
