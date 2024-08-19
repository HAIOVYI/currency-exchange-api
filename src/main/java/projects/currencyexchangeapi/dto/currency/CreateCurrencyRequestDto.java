package projects.currencyexchangeapi.dto.currency;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCurrencyRequestDto(

        @Pattern(regexp = "^[A-Z]{3}$", message = "must be exactly 3 uppercase letters")
        String code,

        @NotNull(message = "is mandatory")
        @Size(max = 50, message = "should not exceed 50 characters")
        String name
) {
}
