package projects.currencyexchangeapi.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.ExchangeEntity;
import projects.currencyexchangeapi.holder.PendingExchangeHolder;
import projects.currencyexchangeapi.holder.WebSessionHolder;
import projects.currencyexchangeapi.mapper.ExchangeMapper;
import projects.currencyexchangeapi.repository.ExchangeRepository;
import projects.currencyexchangeapi.service.ExchangeService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeScheduler {

    @Value("${exchange.response-timeout-interval}")
    private long responseTimeoutInterval;

    private final PendingExchangeHolder pendingExchangeHolder;
    private final WebSessionHolder webSessionHolder;
    private final ObjectMapper objectMapper;
    private final ExchangeRepository exchangeRepository;
    private final ExchangeMapper exchangeMapper;
    private final ExchangeService exchangeService;

    @Scheduled(fixedRateString = "${exchange.check-and-notify-interval}")
    public void notifyPendingRequests() {
        log.info("Starting notifying pending requests");

        for (ExchangeEntity exchange : pendingExchangeHolder.getPendingRequests()) {
            exchange.setRate(exchangeService.getActualRate(exchange));

            if (exchangeService.calculateAvailableBalanceWithReserve(exchange)
                    .compareTo(exchangeService.calculateRequiredAmount(exchange)) >= 0) {
                try {
                    sendMessageToUser(exchangeMapper.toDto(exchange));
                    addReserve(exchange);

                    pendingExchangeHolder.getPendingRequests().remove(exchange);

                    pendingExchangeHolder.getPendingRequestTimers().put(exchange, LocalDateTime.now());

                    log.info("Notified user and removed exchange from pending, exchange id: {}", exchange.getId());
                } catch (Exception e) {
                    log.error("Error notifying user for exchange with id: {}", exchange.getId(), e);
                }
            } else {
                log.info("Skipped exchange from pending, not enough balance for currency, exchange id: {}",
                        exchange.getId());
            }
        }

        log.info("Ending notifying pending requests");
    }

    @Scheduled(fixedRateString = "${exchange.response-timeout-interval}")
    public void handleUnresponsiveUsers() {
        log.info("Starting handle unresponsive users");

        LocalDateTime now = LocalDateTime.now();
        pendingExchangeHolder.getPendingRequestTimers().forEach((exchange, timestamp) -> {
            if (Duration.between(timestamp, now).toMillis() >= responseTimeoutInterval
                    && exchange.getRequestStatus() == ExchangeEntity.RequestStatus.PENDING) {
                exchange.setRequestStatus(ExchangeEntity.RequestStatus.CANCELLED);
                exchangeRepository.save(exchange);
                exchangeService.cancelCurrencyReserve(exchange);
                pendingExchangeHolder.getPendingRequestTimers().remove(exchange);

                log.info("Cancelled exchange due to unresponsive user, exchange id: {}", exchange.getId());
            } else {
                log.info("Skipped cancelling unresponsive exchange, exchange id: {}", exchange.getId());
            }
        });
    }

    private void addReserve(ExchangeEntity exchange) {
        log.info("Adding reserve for currency, currency id: {}", exchange.getCurrencyTo().getId());

        pendingExchangeHolder.getCurrencyReserveMap().computeIfPresent(exchange.getCurrencyTo().getId(),
                (key, value) -> value.add(exchangeService.calculateRequiredAmount(exchange)));

        pendingExchangeHolder.getCurrencyReserveMap().computeIfAbsent(exchange.getCurrencyTo().getId(),
                key -> exchangeService.calculateRequiredAmount(exchange));

        log.info("Added reserve for currency, currency id: {}", exchange.getCurrencyTo().getId());
    }

    private void sendMessageToUser(ExchangeResponseDto responseDto) throws Exception {
        log.info("Sending message to user, user id: {}", responseDto.userId());

        WebSocketSession session = webSessionHolder.getSessionMap().get(responseDto.userId());
        if (session != null && session.isOpen()) {
            String responseMessage = objectMapper.writeValueAsString(responseDto);
            session.sendMessage(new TextMessage(responseMessage));

            log.info("Sent message to user, user id: {}", responseDto.userId());
        } else {
            log.warn("Skipped send message to user, user id: {}", responseDto.userId());
        }
    }
}
