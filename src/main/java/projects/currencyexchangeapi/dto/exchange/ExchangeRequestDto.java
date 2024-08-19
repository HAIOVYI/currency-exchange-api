package projects.currencyexchangeapi.dto.exchange;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ExchangeRequestDto(

        @NotNull
        Long currencyFromId,

        @NotNull
        Long currencyToId,

        @NotNull
        @Positive(message = "must be greater than zero")
        @Min(value = 1, message = "must be at least 1")
        BigDecimal amount
) {
}
