package projects.currencyexchangeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    private static MockMvc mockMvc;

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
                    new ClassPathResource("db/users/remove-user-roles.sql"));
        }
    }

    @Test
    @DisplayName("Block a user with ADMIN role successfully")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql",
            "classpath:db/users/create-default-roles.sql",
            "classpath:db/users/create-user-with-user-role.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void blockUser_AdminRole_SuccessfulBlock() throws Exception {

        Long userId = 1L;

        MvcResult result = mockMvc.perform(put("/user/{userId}/block", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertEquals("User blocked successfully", responseContent);
    }

    @Test
    @DisplayName("Block a user without ADMIN role should return Forbidden")
    @WithMockUser(username = "user")
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users_roles.sql",
            "classpath:db/users/remove-all-users.sql",
            "classpath:db/users/create-default-roles.sql",
            "classpath:db/users/create-user-with-user-role.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/users/remove-user-roles.sql",
            "classpath:db/users/remove-all-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void blockUser_NonAdminRole_Forbidden() throws Exception {

        Long userId = 1L;

        mockMvc.perform(put("/user/{userId}/block", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
