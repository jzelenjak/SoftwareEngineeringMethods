package nl.tudelft.sem.users.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.jwt.JwtUtils;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.repositories.UserRepository;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient MockMvc mockMvc;

    private transient MockWebServer mockWebServer;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;


    // Some constants for JSON fields
    private static final transient String USERID = "userId";
    private static final transient String USERNAME = "username";
    private static final transient String ROLE = "role";
    private static final transient String BEARER = "Bearer ";
    private static final transient String UTF8 = "uft-8";


    // Constants for APIs
    private static final transient String REGISTER_API = "/api/users/register";
    private static final transient String CHANGE_ROLE_API = "/api/users/change_role";
    private static final transient String DELETE_API = "/api/users/delete";

    private final transient String username = "S.Bar@student.tudelft.nl";
    private final transient String firstName = "Sasha";
    private final transient String lastName = "Bar";

    /**
     * Helper methods.
     */

    private String createJson(String... kvPairs) {
        ObjectNode node = new ObjectMapper().createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toString();
    }

    void configureGateway(String path) {
        HttpUrl url = mockWebServer.url(path);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    private void configureJwsMock(String roleToReturn) {
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        Mockito.when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(jwsMock);
        Mockito.when(jwtUtils.getRole(Mockito.any())).thenReturn(roleToReturn);
    }



    /**
     * Helper methods for configuring MockMVC.
     */

    private ResultActions mockMvcRegister(String json) throws Exception {
        return mockMvc.perform(post(REGISTER_API).contentType(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(UTF8));
    }

    private ResultActions mockMvcGetByUsername(String username) throws Exception {
        return mockMvc.perform(get("/api/users/by_username").queryParam("username", username));
    }

    private ResultActions mockMvcGetByUserId(String userId) throws Exception {
        return mockMvc.perform(get("/api/users/by_userid").queryParam("userId", userId));
    }

    private ResultActions mockMvcGetByRole(String role) throws Exception {
        return mockMvc.perform(get("/api/users/by_role").queryParam("role", role));
    }

    private ResultActions mockMvcChangeRole(String json) throws Exception {
        return mockMvc.perform(put(CHANGE_ROLE_API).contentType(MediaType.APPLICATION_JSON)
                .content(json).header(HttpHeaders.AUTHORIZATION, "jwtToken")
                .characterEncoding(UTF8));
    }

    private ResultActions mockMvcDeleteByUserId(String userId) throws Exception {
        return mockMvc.perform(delete(DELETE_API).queryParam("userId", userId)
                .header(HttpHeaders.AUTHORIZATION, "jwtToken"));
    }

    private void assertRecordedRequestNull() throws InterruptedException {
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    private void assertRecordedRequestNotNull(String method) throws InterruptedException {
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions.assertThat(recordedRequest).isNotNull();
        Assertions.assertThat(recordedRequest.getMethod()).isEqualTo(method);
    }

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Tests for registerUser method.
     */

    @Test
    void registerUserAlreadyExistsLocallyTest() throws Exception {
        configureGateway(REGISTER_API);
        userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));

        String json = createJson(USERNAME, username, "firstName", firstName,
                "lastName", lastName, "password", "123");
        mockMvcRegister(json).andExpect(status().isConflict()).andExpect(content().string(""));
        assertRecordedRequestNull();
    }

    @Test
    void registerUserAlreadyExistsFailureInAuthTest() throws Exception {
        configureGateway("/api/auth/register");
        mockWebServer.enqueue(new MockResponse().setResponseCode(409));


        MvcResult mvcResult = mockMvcRegister(createJson(USERNAME, username, "firstName", firstName,
                "lastName", lastName, "password", "123")).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        assertRecordedRequestNotNull("POST");
    }

    @Test
    void registerUserSuccessTest() throws Exception {
        configureGateway("/api/auth/register");
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));


        MvcResult mvcRes = mockMvcRegister(createJson(USERNAME, username,
                "firstName", firstName, "lastName", lastName, "password", "1234")).andReturn();
        mockMvc.perform(asyncDispatch(mvcRes)).andExpect(status().isOk());
        assertRecordedRequestNotNull("POST");
    }


    /**
     * Tests for getByUsername method.
     */


    @Test
    void getByUsernameNotFoundTest() throws Exception {
        mockMvcGetByUsername(username).andExpect(status().isNotFound());
    }

    @Test
    void getByUsernameFoundTest() throws Exception {
        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));


        String result = mockMvcGetByUsername(username).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        User received = new ObjectMapper().readValue(result, User.class);
        Assertions.assertThat(received).isEqualTo(user);
    }


    /**
     * Tests for getByUserId method.
     */


    @Test
    void getByUserIdNotFoundTest() throws Exception {
        mockMvcGetByUserId(String.valueOf(456789)).andExpect(status().isNotFound());
    }

    @Test
    void getByUserIdFoundTest() throws Exception {
        User user = userRepository.save(new User(username, firstName, lastName, UserRole.LECTURER));
        long userId = user.getUserId();

        String res = mockMvcGetByUserId(String.valueOf(userId)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        User received = new ObjectMapper().readValue(res, User.class);
        Assertions.assertThat(received).isEqualTo(user);
    }


    /**
     * Tests for getByRole method.
     */


    @Test
    void getByRoleNotFoundTest() throws Exception {
        mockMvcGetByRole(UserRole.TA.name()).andExpect(status().isNotFound());
    }

    @Test
    void getByRoleFoundTest() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT)));
        users.add(userRepository.save(new User("...", firstName, lastName, UserRole.STUDENT)));
        users.add(userRepository.save(new User("..", "ghi", "jkl", UserRole.STUDENT)));
        userRepository.save(new User(".", firstName, lastName, UserRole.LECTURER));

        String res = mockMvcGetByRole(UserRole.STUDENT.name()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<User> received = new ObjectMapper().readValue(res, new TypeReference<List<User>>(){});
        Assertions.assertThat(received).isEqualTo(users);
    }


    /**
     * Tests for changeRole method.
     */


    @Test
    void changeRoleInvalidOrExpiredTokenTest() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        Mockito.when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(null);

        String json = new ObjectMapper().createObjectNode().put(USERID, 5422341L)
                .put(ROLE, UserRole.TA.name()).toString();


        mockMvcChangeRole(json).andExpect(status().isUnauthorized());
        assertRecordedRequestNull();
    }

    @Test
    void changeRoleTokenDoesNotStartWithBearerTest() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn(null);

        String json = new ObjectMapper().createObjectNode().put(USERID, 3456774L)
                .put(ROLE, UserRole.TA.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isUnauthorized());
        assertRecordedRequestNull();
    }

    @Test
    void changeRoleUnauthorizedTest() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.TA.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 2376889L)
                .put(ROLE, UserRole.TA.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isUnauthorized());
        assertRecordedRequestNull();
    }

    @Test
    void changeRoleFailureAtAuthTest() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));
        String json = new ObjectMapper().createObjectNode().put(USERID, user.getUserId())
                .put(ROLE, UserRole.TA.name()).toString();

        MvcResult mvcResult = mockMvcChangeRole(json).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());

        assertRecordedRequestNotNull("PUT");
    }

    @Test
    void changeRoleSuccessfulTest() throws Exception {
        configureJwsMock(UserRole.LECTURER.name());
        configureGateway(CHANGE_ROLE_API);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));
        String json = new ObjectMapper().createObjectNode().put(USERID, user.getUserId())
                .put(ROLE, UserRole.TA.name()).toString();

        MvcResult mvcResult = mockMvcChangeRole(json).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
        assertRecordedRequestNotNull("PUT");
    }


    /**
     * Tests for deleteByUserId method.
     */


    @Test
    void deleteByUserIdUnauthorizedTest() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.STUDENT.name());

        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));

        mockMvcDeleteByUserId("" + user.getUserId()).andExpect(status().isUnauthorized());
        assertRecordedRequestNull();
    }

    @Test
    void deleteByUserIdFailureAtAuthTest() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));

        MvcResult mvcResult =  mockMvcDeleteByUserId("" + user.getUserId()).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
        assertRecordedRequestNotNull("DELETE");
    }

    @Test
    void deleteByUserIdSuccessfulTest() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        User user = userRepository.save(new User(username, firstName, lastName, UserRole.STUDENT));

        MvcResult mvcResult = mockMvcDeleteByUserId("" + user.getUserId()).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
        assertRecordedRequestNotNull("DELETE");
    }


    /**
     * Tests for edge cases or exceptions.
     */

    @Test
    void notNumberTest() throws Exception {
        mockMvcGetByUserId("nan").andExpect(status().isBadRequest());
    }

    /**
     * Remaining tests for 100% coverage.
     */
    @Test
    void gatewayConfigHostTest() {
        GatewayConfig config = new GatewayConfig();
        config.setHost("google.com");
        Assertions.assertThat(config.getHost()).isEqualTo("google.com");
    }

    @Test
    void gatewayConfigPortTest() {
        GatewayConfig config = new GatewayConfig();
        config.setPort(8089);
        Assertions.assertThat(config.getPort()).isEqualTo(8089);
    }

    @Test
    void missingValuesInJsonTest() throws Exception {
        mockMvcRegister(createJson("ID", "4432894")).andExpect(status().isBadRequest());
    }


    @Test
    void invalidRoleTest() throws Exception {
        mockMvcGetByRole("MODERATOR").andExpect(status().isBadRequest());
    }

    @Test
    void invalidJwsRole() throws Exception {
        configureJwsMock("MODERATOR");

        String json = new ObjectMapper().createObjectNode().put(USERID, 34126654L)
                .put(ROLE, UserRole.TA.name()).toString();
        mockMvcChangeRole(json).andExpect(status().isBadRequest());
    }
}