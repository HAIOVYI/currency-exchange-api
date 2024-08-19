package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyRateControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(@Autowired javax.sql.DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/currencies/remove-all-currencies.sql"));
        }
    }

    @Sql(scripts = {"classpath:db/currency_rates/add-default-currencies.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    @DisplayName("Given available currencies, retrieve the current exchange rate between two currencies")
    public void getNextAvailableCurrencies_CurrenciesAvailable_ReturnsExpectedRate() throws Exception {

        CurrencyRateResponseDto expected = new CurrencyRateResponseDto(
                new BigDecimal("44.5000"));

        String fromCurrency = "USD";
        String toCurrency = "UAH";

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/currency-rate/current?fromCurrency={from}&toCurrency={to}",
                String.class,
                fromCurrency,
                toCurrency);

        CurrencyRateResponseDto actual = objectMapper.readValue(response.getBody(), CurrencyRateResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected.rate(), actual.rate().setScale(4, RoundingMode.HALF_UP));
    }
}
