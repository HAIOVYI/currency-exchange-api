package projects.currencyexchangeapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@Table(name = "exchange_requests")
public class ExchangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "currency_from_id", nullable = false)
    private CurrencyEntity currencyFrom;

    @ManyToOne
    @JoinColumn(name = "currency_to_id", nullable = false)
    private CurrencyEntity currencyTo;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal rate;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PENDING', 'COMPLETED', 'CANCELLED')")
    private RequestStatus requestStatus = RequestStatus.PENDING;

    public enum RequestStatus {
        PENDING, COMPLETED, CANCELLED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExchangeEntity exchange = (ExchangeEntity) o;
        return Objects.equals(id, exchange.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
