package projects.currencyexchangeapi.holder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import org.springframework.stereotype.Component;
import projects.currencyexchangeapi.entity.ExchangeEntity;

@Component
@Getter
public class PendingExchangeHolder {

    private final Map<ExchangeEntity, LocalDateTime> pendingRequestTimers = new ConcurrentHashMap<>();
    private final Map<Long, BigDecimal> currencyReserveMap = new ConcurrentHashMap<>();
    private final Queue<ExchangeEntity> pendingRequests = new ConcurrentLinkedQueue<>();
}
