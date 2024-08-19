package projects.currencyexchangeapi.dto.exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import projects.currencyexchangeapi.entity.ExchangeEntity;

public record ExchangeResponseDto(

        Long id,

        Long userId,

        Long currencyFromId,

        Long currencyToId,

        BigDecimal amount,

        BigDecimal rate,

        LocalDateTime timestamp,

        ExchangeEntity.RequestStatus requestStatus
) {
}
