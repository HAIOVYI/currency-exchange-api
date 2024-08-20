package projects.currencyexchangeapi.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projects.currencyexchangeapi.entity.CurrencyRateEntity;
import projects.currencyexchangeapi.entity.CurrencyRateProjection;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRateEntity, Long> {

    @Query(nativeQuery = true, value = """
            SELECT (SELECT rate
                    FROM currency_rates
                    WHERE currency_id = (SELECT id FROM currencies WHERE code = :toCurrency)
                    ORDER BY timestamp DESC
                    LIMIT 1) /
                   (SELECT rate
                    FROM currency_rates
                    WHERE currency_id = (SELECT id FROM currencies WHERE code = :fromCurrency)
                    ORDER BY timestamp DESC
                    LIMIT 1)
                       AS `rate`
            """)
    BigDecimal getCurrentRate(@Param("fromCurrency") String fromCurrency,
                              @Param("toCurrency") String toCurrency);

    @Query(nativeQuery = true, value = """
            SELECT (SELECT (SELECT MIN(rate)
                            FROM currency_rates
                            WHERE currency_id = (SELECT id FROM currencies WHERE code = :toCurrency)
                              AND timestamp BETWEEN :fromDate AND :toDate) /
                           (SELECT AVG(rate)
                            FROM currency_rates
                            WHERE currency_id = (SELECT id FROM currencies WHERE code = :fromCurrency)
                              AND timestamp BETWEEN :fromDate AND :toDate)) AS `minRate`,
                   (SELECT (SELECT MAX(rate)
                            FROM currency_rates
                            WHERE currency_id = (SELECT id FROM currencies WHERE code = :toCurrency)
                              AND timestamp BETWEEN :fromDate AND :toDate) /
                           (SELECT AVG(rate)
                            FROM currency_rates
                            WHERE currency_id = (SELECT id FROM currencies WHERE code = :fromCurrency)
                              AND timestamp BETWEEN :fromDate AND :toDate)) AS `maxRate`
            """)
    CurrencyRateProjection getRate(@Param("fromCurrency") String fromCurrency,
                                   @Param("toCurrency") String toCurrency,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    @Query(nativeQuery = true, value = """
            SELECT rates.id, rates.currency_id, rates.rate / base_rate.rate AS rate, rates.timestamp
            FROM currency_rates rates
            JOIN (
                SELECT currency_id, rate
                FROM currency_rates
                WHERE currency_id = (SELECT id FROM currencies WHERE code = :baseCurrency)
                ORDER BY timestamp DESC
                LIMIT 1
            ) base_rate
            WHERE rates.timestamp = (SELECT MAX(rates2.timestamp)
                                     FROM currency_rates rates2
                                     WHERE rates2.currency_id = rates.currency_id)
            ORDER BY rates.currency_id;
                            """)
    List<CurrencyRateEntity> findAllByBaseCurrency(@Param("baseCurrency") String baseCurrency);
}
