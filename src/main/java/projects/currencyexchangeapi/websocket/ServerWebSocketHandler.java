package projects.currencyexchangeapi.websocket;

import static projects.currencyexchangeapi.security.JwtUtil.AUTHORIZATION_HEADER;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import projects.currencyexchangeapi.dto.exchange.ConfirmExchangeRequestDto;
import projects.currencyexchangeapi.entity.UserEntity;
import projects.currencyexchangeapi.exception.InvalidTokenException;
import projects.currencyexchangeapi.holder.WebSessionHolder;
import projects.currencyexchangeapi.security.JwtUtil;
import projects.currencyexchangeapi.service.ExchangeService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final WebSessionHolder sessionHolder;
    private final ExchangeService exchangeService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Received message from session id: {}", session.getId());

        try {
            String request = message.getPayload();
            ConfirmExchangeRequestDto confirmExchangeRequestDto = objectMapper.readValue(
                    request, ConfirmExchangeRequestDto.class);

            UserEntity user = getUser(session.getHandshakeHeaders());

            String responseAsString = objectMapper.writeValueAsString(
                    exchangeService.confirm(confirmExchangeRequestDto, user));

            log.info("Processing exchange confirmation for user id: {}", user.getId());

            session.sendMessage(new TextMessage(responseAsString));
            log.info("Sent confirmation response to session id: {}", session.getId());
        } catch (Exception e) {
            log.error("Error processing message in session id: {}, error: {}",
                    session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connection established, session id: {}", session.getId());

        try {
            Long userId = getUser(session.getHandshakeHeaders()).getId();
            sessionHolder.getSessionMap().put(userId, session);
        } catch (Exception e) {
            log.error("Error establishing connection for session id: {}, error: {}",
                    session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed, session id: {}, status: {}", session.getId(), status);

        try {
            Long userId = getUser(session.getHandshakeHeaders()).getId();
            sessionHolder.getSessionMap().remove(userId);

            log.info("User id: {} disconnected, session id: {}", userId, session.getId());
        } catch (Exception e) {
            log.error("Error closing connection for session id: {}, error: {}",
                    session.getId(), e.getMessage(), e);
        }
    }

    private UserEntity getUser(HttpHeaders httpHeaders) {
        String bearerToken = httpHeaders.getFirst(AUTHORIZATION_HEADER);

        String token = jwtUtil.extractToken(bearerToken);

        if (token != null && jwtUtil.isValidToken(token)) {
            String email = jwtUtil.getUserEmail(token);

            log.info("Authenticated user");

            UserEntity user = (UserEntity) userDetailsService.loadUserByUsername(email);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return user;
        }

        throw new InvalidTokenException("Invalid token");
    }
}
