package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import projects.currencyexchangeapi.dto.login.UserLoginRequestDto;
import projects.currencyexchangeapi.dto.login.UserLoginResponseDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterRequestDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterResponseDto;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {

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
        }
    }

    @Test
    @DisplayName("Register a new user successfully")
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users_roles.sql",
            "classpath:db/users/remove-all-users.sql",
            "classpath:db/users/create-default-roles.sql",
            "classpath:db/users/create-user-with-user-role.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void registerUser_SuccessfulRegistration() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
                "test@gmail.com",
                "password123",
                "password123",
                "test",
                "test");

        UserRegisterResponseDto expected = new UserRegisterResponseDto(
                1L,
                requestDto.email(),
                requestDto.firstName(),
                requestDto.lastName());

        MvcResult result = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        UserRegisterResponseDto actualResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserRegisterResponseDto.class);

        assertEquals(expected.email(), actualResponse.email());
        assertEquals(expected.firstName(), actualResponse.firstName());
        assertEquals(expected.lastName(), actualResponse.lastName());
    }

    @Test
    @DisplayName("Login with valid credentials successfully")
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users_roles.sql",
            "classpath:db/users/remove-all-users.sql",
            "classpath:db/users/create-default-roles.sql",
            "classpath:db/users/create-user-with-user-role.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void loginUser_ValidCredentials_SuccessfulLogin() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "default@gmail.com",
                "user1user1");

        String expected = "eyJhbGc";

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserLoginResponseDto.class);

        assertTrue(actual.token().startsWith(expected));
    }
}
