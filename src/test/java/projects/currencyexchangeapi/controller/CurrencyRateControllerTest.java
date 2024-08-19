package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
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
import projects.currencyexchangeapi.dto.rate.CurrencyRateResponseDto;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyRateControllerTest {

    protected static MockMvc mockMvc;

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
                    new ClassPathResource("db/users/add-user-roles.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/currencies/clear-currencies-and-rates.sql"));
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
                    new ClassPathResource("db/currencies/clear-currencies-and-rates.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/users/remove-user-roles.sql"));
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {"classpath:db/currency_rates/add-default-currencies.sql",
            "classpath:db/currency_rates/add-default-currency-rates.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    @DisplayName("Given available currencies, retrieve the current exchange rate between two currencies")
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
}
