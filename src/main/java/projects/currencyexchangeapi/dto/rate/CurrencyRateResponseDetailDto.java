package projects.currencyexchangeapi.dto.rate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CurrencyRateResponseDetailDto(

        Long id,

        Long currencyId,

        String currencyCode,

        String currencyName,

        BigDecimal rate,

        LocalDateTime timestamp
) {
}
