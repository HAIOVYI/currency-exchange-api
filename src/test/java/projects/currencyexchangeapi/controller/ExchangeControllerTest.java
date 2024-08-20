package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import projects.currencyexchangeapi.dto.exchange.ExchangeRequestDto;
import projects.currencyexchangeapi.dto.exchange.ExchangeResponseDto;
import projects.currencyexchangeapi.entity.ExchangeEntity;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExchangeControllerTest {

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
                    new ClassPathResource("db/currencies/remove-all-currencies-and-currency-rates.sql"));
        }
    }

    @Test
    @DisplayName("Create a new exchange request successfully")
    @WithUserDetails("default@gmail.com")
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users_roles.sql",
            "classpath:db/users/remove-all-users.sql",
            "classpath:db/users/create-default-roles.sql",
            "classpath:db/users/create-user-with-user-role.sql",
            "classpath:db/currency_rates/create-default-currencies.sql",
            "classpath:db/currency_rates/create-currency-rates-for-history.sql",
            "classpath:db/exchange/set-currency-balance.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/exchange/remove-all-exchange-requests.sql",
            "classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createExchangeRequest_SuccessfulCreation() throws Exception {
        ExchangeRequestDto requestDto = new ExchangeRequestDto(
                2L,
                1L,
                new BigDecimal("100.0000"));

        ExchangeResponseDto expected = new ExchangeResponseDto(
                1L,
                1L,
                requestDto.currencyFromId(),
                requestDto.currencyToId(),
                requestDto.amount(),
                new BigDecimal("43.5430"),
                LocalDateTime.now(),
                ExchangeEntity.RequestStatus.COMPLETED);

        MvcResult result = mockMvc.perform(post("/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ExchangeResponseDto actualResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExchangeResponseDto.class);

        assertEquals(expected.id(), actualResponse.id());
        assertEquals(expected.currencyFromId(), actualResponse.currencyFromId());
        assertEquals(expected.currencyToId(), actualResponse.currencyToId());
        assertEquals(expected.amount(), actualResponse.amount().setScale(4, RoundingMode.HALF_UP));
        assertEquals(expected.rate(), actualResponse.rate().setScale(4, RoundingMode.HALF_UP));
        assertEquals(expected.requestStatus(), actualResponse.requestStatus());
    }
}
