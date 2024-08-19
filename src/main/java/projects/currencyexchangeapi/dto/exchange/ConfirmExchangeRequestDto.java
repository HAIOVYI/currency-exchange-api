package projects.currencyexchangeapi.dto.exchange;

import jakarta.validation.constraints.NotNull;

public record ConfirmExchangeRequestDto(

        @NotNull
        Long exchangeId,

        @NotNull
        boolean confirmed
) {
}
