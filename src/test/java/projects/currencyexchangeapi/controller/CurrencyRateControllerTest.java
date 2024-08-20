package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import projects.currencyexchangeapi.dto.rate.CreateCurrencyRateRequestDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDetailDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;
import projects.currencyexchangeapi.dto.rate.CurrencyRetrospectiveResponseDto;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyRateControllerTest {

    private static final BigDecimal MIN_RATE = new BigDecimal("0.8957");
    private static final BigDecimal MAX_RATE = new BigDecimal("0.9557");

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext,
                          @Autowired DataSource dataSource) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/users/create-default-roles.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/currencies/remove-all-currencies-and-currency-rates.sql"));
        }
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
                    new ClassPathResource("db/currency_rates/remove-default-currencies.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/users/remove-user-roles.sql"));
        }
    }

    @Test
    @DisplayName("Given available currencies, retrieve the current exchange rate between two currencies")
    @WithMockUser(username = "user")
    @Sql(scripts = {"classpath:db/currency_rates/create-default-currencies.sql",
            "classpath:db/currency_rates/create-default-currency-rates.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getNextAvailableCurrencies_CurrenciesAvailable_ReturnsExpectedRate() throws Exception {
        CurrencyRateResponseDto expected = new CurrencyRateResponseDto(
                new BigDecimal("44.5000"));

        String fromCurrency = "USD";
        String toCurrency = "UAH";

        MvcResult result = mockMvc.perform(get("/currency-rate/current")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CurrencyRateResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CurrencyRateResponseDto.class);

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(expected.rate(), actual.rate().setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Given available currencies and date range, retrieve historical exchange rates between two currencies")
    @WithMockUser(username = "user")
    @Sql(scripts = {"classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            "classpath:db/currency_rates/create-default-currencies.sql",
            "classpath:db/currency_rates/create-currency-rates-for-history.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getRateHistory_CurrenciesAvailable_ReturnsExpectedRates() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        LocalDate fromDate = LocalDate.now().minusWeeks(1);
        LocalDate toDate = LocalDate.now();

        MvcResult result = mockMvc.perform(get("/currency-rate/history")
                        .param("fromCurrency", fromCurrency)
                        .param("toCurrency", toCurrency)
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CurrencyRetrospectiveResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CurrencyRetrospectiveResponseDto.class);

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(MIN_RATE, actual.minRate().setScale(4, RoundingMode.HALF_UP));
        assertEquals(MAX_RATE, actual.maxRate().setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Given a base currency, retrieve its exchange rates against other currencies")
    @WithMockUser(username = "user")
    @Sql(scripts = {"classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            "classpath:db/currency_rates/create-default-currencies.sql",
            "classpath:db/currency_rates/create-currency-rates-for-history.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getBaseRates_ValidBaseCurrency_ReturnsExpectedRates() throws Exception {
        List<CurrencyRateResponseDetailDto> expected = new ArrayList<>();

        expected.add(new CurrencyRateResponseDetailDto(
                1L,
                1L,
                "UAH",
                "UAH currency",
                new BigDecimal("43.5430"),
                LocalDateTime.now()));

        expected.add(new CurrencyRateResponseDetailDto(
                2L,
                2L,
                "USD",
                "USD currency",
                new BigDecimal("1.000"),
                LocalDateTime.now()));

        expected.add(new CurrencyRateResponseDetailDto(
                3L,
                3L,
                "EUR",
                "EUR currency",
                new BigDecimal("0.9557"),
                LocalDateTime.now()));

        String baseCurrency = "USD";

        MvcResult result = mockMvc.perform(get("/currency-rate/base")
                        .param("baseCurrency", baseCurrency)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CurrencyRateResponseDetailDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CurrencyRateResponseDetailDto[].class);

        assertEquals(expected.size(), actual.length);

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).currencyId(), actual[i].currencyId());
            assertEquals(expected.get(i).currencyCode(), actual[i].currencyCode());
            assertEquals(0, expected.get(i).rate().compareTo(actual[i].rate()));
        }
    }

    @Test
    @DisplayName("Create a new currency rate when the user has ADMIN role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {"classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            "classpath:/db/currencies/create-three-test-currencies.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/currencies/remove-all-currencies-and-currency-rates.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createCurrencyRate_AdminRole_SuccessfulCreation() throws Exception {
        CreateCurrencyRateRequestDto expected = new CreateCurrencyRateRequestDto(
                1L,
                new BigDecimal("42.5000"));

        MvcResult result = mockMvc.perform(post("/currency-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expected)))
                .andExpect(status().isCreated())
                .andReturn();

        CurrencyRateResponseDetailDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CurrencyRateResponseDetailDto.class);

        assertEquals(expected.currencyId(), actual.currencyId());
        assertEquals(expected.rate(), actual.rate());
    }
}
