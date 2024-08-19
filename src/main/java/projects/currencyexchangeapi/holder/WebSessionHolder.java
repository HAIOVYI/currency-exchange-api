package projects.currencyexchangeapi.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSessionHolder {

    @Getter
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

}
