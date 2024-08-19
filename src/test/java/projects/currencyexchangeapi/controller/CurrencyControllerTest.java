package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
import projects.currencyexchangeapi.dto.currency.CreateCurrencyRequestDto;
import projects.currencyexchangeapi.dto.currency.CurrencyResponseDto;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyControllerTest {

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
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(@Autowired DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/users/remove-user-roles.sql"));
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = "classpath:db/currencies/remove-test-currency.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Create a new currency")
    void createNewCurrency_ValidRequestDto_Success() throws Exception {
        String currencyCode = "KKK";
        String currencyName = "KKK currency";
        CreateCurrencyRequestDto requestDto = new CreateCurrencyRequestDto(currencyCode, currencyName);

        Long currencyId = anyLong();
        CurrencyResponseDto expected = new CurrencyResponseDto(currencyId, requestDto.code(), requestDto.name());

        MvcResult result = mockMvc.perform(post("/currency")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CurrencyResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CurrencyResponseDto.class);

        assertNotNull(actual.id());

        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {"classpath:db/currencies/clear-currencies-and-rates.sql",
            "classpath:db/currencies/add-three-test-currencies.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/currencies/remove-three-test-currencies.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Get all currencies")
    void getAll_GivenCurrencies_ShouldReturnAllCurrencies() throws Exception {
        List<CurrencyResponseDto> expected = new ArrayList<>();
        expected.add(new CurrencyResponseDto(1L, "ZZZ", "ZZZ currency"));
        expected.add(new CurrencyResponseDto(2L, "MMM", "MMM currency"));
        expected.add(new CurrencyResponseDto(3L, "VVV", "VVV currency"));

        MvcResult result = mockMvc.perform(get("/currency")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CurrencyResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CurrencyResponseDto[].class);

        assertEquals(3, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }
}
