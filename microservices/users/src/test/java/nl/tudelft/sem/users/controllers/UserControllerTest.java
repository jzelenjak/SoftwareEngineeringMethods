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
import java.util.Optional;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
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
    private static final transient String FIRSTNAME = "firstName";
    private static final transient String LASTNAME = "lastName";
    private static final transient String ROLE = "role";
    private static final transient String UTF8 = "uft-8";

    // Constants for APIs
    private static final transient String REGISTER_API = "/api/users/register";
    private static final transient String CHANGE_ROLE_API = "/api/users/change_role";
    private static final transient String CHANGE_FIRST_NAME_API = "/api/users/change_first_name";
    private static final transient String CHANGE_LAST_NAME_API = "/api/users/change_last_name";
    private static final transient String DELETE_API = "/api/users/delete";

    // Common used data constants
    private final transient String username = "sbar";
    private final transient String firstNameStudent = "Sasha";
    private final transient String newFirstName = "Stan";
    private final transient String lastNameStudent = "Bar";
    private final transient String newLastName = "Lee";
    private final transient String jwt = "Bearer someValidJwt";


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
        return mockMvc.perform(get("/api/users/by_username").header(HttpHeaders.AUTHORIZATION, jwt)
                                    .queryParam("username", username));
    }

    private ResultActions mockMvcGetByUserId(String userId) throws Exception {
        return mockMvc.perform(get("/api/users/by_userid").header(HttpHeaders.AUTHORIZATION, jwt)
                                    .queryParam("userId", userId));
    }

    private ResultActions mockMvcGetByRole(String role) throws Exception {
        return mockMvc.perform(get("/api/users/by_role").header(HttpHeaders.AUTHORIZATION, jwt)
                                    .queryParam("role", role));
    }

    private ResultActions mockMvcGetByFirstName(String firstName) throws Exception {
        return mockMvc.perform(get("/api/users/by_first_name")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .queryParam(FIRSTNAME, firstName));
    }

    private ResultActions mockMvcGetByLastName(String lastName) throws Exception {
        return mockMvc.perform(get("/api/users/by_last_name")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .queryParam(LASTNAME, lastName));
    }

    private ResultActions mockMvcChangeRole(String json) throws Exception {
        return mockMvc.perform(put(CHANGE_ROLE_API).contentType(MediaType.APPLICATION_JSON)
                .content(json).header(HttpHeaders.AUTHORIZATION, jwt)
                .characterEncoding(UTF8));
    }

    private ResultActions mockMvcChangeFirstName(String json) throws Exception {
        return mockMvc.perform(put(CHANGE_FIRST_NAME_API).contentType(MediaType.APPLICATION_JSON)
                .content(json).header(HttpHeaders.AUTHORIZATION, jwt)
                .characterEncoding(UTF8));
    }

    private ResultActions mockMvcChangeLastName(String json) throws Exception {
        return mockMvc.perform(put(CHANGE_LAST_NAME_API).contentType(MediaType.APPLICATION_JSON)
                .content(json).header(HttpHeaders.AUTHORIZATION, jwt)
                .characterEncoding(UTF8));
    }

    private ResultActions mockMvcDeleteByUserId(String userId) throws Exception {
        return mockMvc.perform(delete(DELETE_API).header(HttpHeaders.AUTHORIZATION, jwt)
                                    .queryParam("userId", userId));
    }

    private void assertRecordedRequestNull() throws InterruptedException {
        Assertions.assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS)).isNull();
    }

    private void assertRecordedRequestWithJwt(RecordedRequest recordedRequest, HttpMethod method) {
        assertRecordedRequestNoJwt(recordedRequest, method);
        Assertions.assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo(jwt);
    }

    private void assertRecordedRequestNoJwt(RecordedRequest recordedRequest, HttpMethod method) {
        Assertions.assertThat(recordedRequest).isNotNull();
        Assertions.assertThat(recordedRequest.getMethod()).isEqualTo(method.name());
    }


    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    /**
     * Tests for registerUser method.
     */

    @Test
    void testRegisterUserAlreadyExistsLocally() throws Exception {
        configureGateway(REGISTER_API);
        userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));

        String json = createJson(USERNAME, username, FIRSTNAME, firstNameStudent,
                "lastName", lastNameStudent, "password", "123");
        mockMvcRegister(json).andExpect(status().isConflict())
            .andExpect(content().string("User with username " + username + " already exist"));
        assertRecordedRequestNull();
    }

    @Test
    void testRegisterUserAlreadyExistsFailureInAuth() throws Exception {
        configureGateway("/api/auth/register");
        mockWebServer.enqueue(new MockResponse().setResponseCode(409));

        MvcResult mvcResult = mockMvcRegister(createJson(USERNAME, username,
                FIRSTNAME, firstNameStudent, "lastName", lastNameStudent,
                "password", "123")).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestNoJwt(recordedRequest, HttpMethod.POST);
    }

    @Test
    void testRegisterUserSuccessful() throws Exception {
        configureGateway("/api/auth/register");
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        MvcResult mvcRes = mockMvcRegister(createJson(USERNAME, username,
                FIRSTNAME, firstNameStudent, LASTNAME, lastNameStudent,
                "password", "1234")).andReturn();
        mockMvc.perform(asyncDispatch(mvcRes)).andExpect(status().isOk());
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestNoJwt(recordedRequest, HttpMethod.POST);
    }


    /**
     * Tests for getByUsername method.
     */

    @Test
    void testGetByUsernameStudentsMustBeForbidden() throws Exception {
        configureJwsMock(UserRole.STUDENT.name());
        mockMvcGetByUsername(username).andExpect(status().isForbidden());
    }

    @Test
    void testGetByUsernameNotFound() throws Exception {
        configureJwsMock(UserRole.LECTURER.name());
        mockMvcGetByUsername(username).andExpect(status().isNotFound());
    }

    @Test
    void testGetByUsernameFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());
        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));

        String result = mockMvcGetByUsername(username).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString();
        User received = new ObjectMapper().readValue(result, User.class);
        Assertions.assertThat(received).isEqualTo(user);
    }


    /**
     * Tests for getByUserId method.
     */

    @Test
    void testGetByUserIdStudentsMustBeForbidden() throws Exception {
        configureJwsMock(UserRole.STUDENT.name());
        mockMvcGetByUserId(String.valueOf(4242442L)).andExpect(status().isForbidden());
    }

    @Test
    void testGetByUserIdNotFound() throws Exception {
        configureJwsMock(UserRole.LECTURER.name());
        mockMvcGetByUserId(String.valueOf(4567899L)).andExpect(status().isNotFound());
    }

    @Test
    void testGetByUserIdFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());
        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.LECTURER));
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
    void testGetByRoleStudentsMustBeForbidden() throws Exception {
        configureJwsMock(UserRole.STUDENT.name());
        mockMvcGetByRole(UserRole.ADMIN.name()).andExpect(status().isForbidden());
    }

    @Test
    void testGetByRoleNotFound() throws Exception {
        configureJwsMock(UserRole.LECTURER.name());
        mockMvcGetByRole(UserRole.ADMIN.name()).andExpect(status().isNotFound());
    }

    @Test
    void testGetByRoleFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());

        List<User> users = new ArrayList<>();
        users.add(userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT)));
        users.add(userRepository.save(new User("...", firstNameStudent,
                lastNameStudent, UserRole.STUDENT)));
        users.add(userRepository.save(new User("..", "ghi", "jkl", UserRole.STUDENT)));
        userRepository.save(new User(".", firstNameStudent, lastNameStudent, UserRole.LECTURER));

        String res = mockMvcGetByRole(UserRole.STUDENT.name()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<User> received = new ObjectMapper().readValue(res, new TypeReference<List<User>>(){});
        Assertions.assertThat(received).isEqualTo(users);
    }


    /**
     * Tests for getByFirstName method.
     */

    @Test
    void testGetByFirstNameStudentsMustBeForbidden() throws Exception {
        configureJwsMock(UserRole.STUDENT.name());
        mockMvcGetByFirstName("Andy").andExpect(status().isForbidden());
    }

    @Test
    void testGetByFirstNameNotFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());
        // First name should not exist (very unlikely)
        mockMvcGetByFirstName("Ahfoefbfpqo").andExpect(status().isNotFound());
    }

    @Test
    void testGetByFirstNameFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());

        List<User> users = new ArrayList<>();
        users.add(userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT)));
        users.add(userRepository.save(new User("skok", firstNameStudent,
                "Kok", UserRole.STUDENT)));
        users.add(userRepository.save(new User("swater", firstNameStudent,
                "Water", UserRole.STUDENT)));
        userRepository.save(new User("sbar1", "Stefan", lastNameStudent, UserRole.STUDENT));

        String res = mockMvcGetByFirstName(firstNameStudent).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<User> received = new ObjectMapper().readValue(res, new TypeReference<>(){});
        Assertions.assertThat(received).isEqualTo(users);
    }


    /**
     * Tests for getByLastName method.
     */

    @Test
    void testGetByLastNameStudentsMustBeForbidden() throws Exception {
        configureJwsMock(UserRole.STUDENT.name());
        mockMvcGetByLastName("Lee").andExpect(status().isForbidden());
    }

    @Test
    void testGetByLastNameNotFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());
        // Last name should not exist (very unlikely)
        mockMvcGetByLastName("Ahfoefbfpqo").andExpect(status().isNotFound());
    }

    @Test
    void testGetByLastNameFound() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());

        List<User> users = new ArrayList<>();
        users.add(userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT)));
        users.add(userRepository.save(new User("bbar", "Bob",
                lastNameStudent, UserRole.STUDENT)));
        users.add(userRepository.save(new User("cbar", "Charlie",
                lastNameStudent, UserRole.STUDENT)));
        userRepository.save(new User("blee", "Bruce", "Lee", UserRole.LECTURER));

        String res = mockMvcGetByLastName(lastNameStudent).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<User> received = new ObjectMapper().readValue(res, new TypeReference<>(){});
        Assertions.assertThat(received).isEqualTo(users);
    }


    /**
     * Tests for changeRole method.
     */

    @Test
    void testChangeRoleInvalidOrExpiredToken() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        Mockito.when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(null);

        String json = new ObjectMapper().createObjectNode().put(USERID, 5422341L)
                            .put(ROLE, UserRole.STUDENT.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeRoleTokenDoesNotStartWithBearer() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        Mockito.when(jwtUtils.resolveToken(Mockito.any())).thenReturn(null);

        String json = new ObjectMapper().createObjectNode().put(USERID, 3456774L)
                            .put(ROLE, UserRole.STUDENT.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isUnauthorized());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeRoleStudentsMustBeForbidden() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.STUDENT.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 2376889L)
                            .put(ROLE, UserRole.LECTURER.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeRoleLecturersMustBeForbidden() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.LECTURER.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 2376889L)
                            .put(ROLE, UserRole.LECTURER.name()).toString();

        mockMvcChangeRole(json).andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeRoleFailureAtAuth() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));
        String json = new ObjectMapper().createObjectNode().put(USERID, user.getUserId())
                            .put(ROLE, UserRole.LECTURER.name()).toString();

        MvcResult mvcResult = mockMvcChangeRole(json).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestWithJwt(recordedRequest, HttpMethod.PUT);
    }

    @Test
    void testChangeRoleSuccessful() throws Exception {
        configureGateway(CHANGE_ROLE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));
        String json = new ObjectMapper().createObjectNode().put(USERID, user.getUserId())
                            .put(ROLE, UserRole.ADMIN.name()).toString();

        MvcResult mvcResult = mockMvcChangeRole(json).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestWithJwt(recordedRequest, HttpMethod.PUT);
    }


    /**
     * Tests for changeFirstName method.
     */
    @Test
    void testChangeFirstNameStudentsMustBeForbidden() throws Exception {
        configureGateway(CHANGE_FIRST_NAME_API);
        configureJwsMock(UserRole.STUDENT.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 19350204L)
                .put(FIRSTNAME, firstNameStudent).toString();

        mockMvcChangeFirstName(json).andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeFirstNameNotFound() throws Exception {
        configureGateway(CHANGE_FIRST_NAME_API);
        configureJwsMock(UserRole.ADMIN.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 6969669L)
            .put(FIRSTNAME, newFirstName).toString();

        mockMvcChangeFirstName(json).andExpect(status().isNotFound());
    }

    @Test
    void testChangeFirstNameSuccessful() throws Exception {
        configureGateway(CHANGE_FIRST_NAME_API);
        configureJwsMock(UserRole.ADMIN.name());

        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));
        final long userId = user.getUserId();
        String json = new ObjectMapper().createObjectNode().put(USERID, userId)
                .put(FIRSTNAME, newFirstName).toString();

        mockMvcChangeFirstName(json).andExpect(status().isOk());

        Optional<User> optionalUser = this.userRepository.findByUserId(userId);
        assert optionalUser.isPresent();
        Assertions.assertThat(optionalUser.get().getFirstName()).isEqualTo(newFirstName);

        this.userRepository.deleteById(userId);
    }

    /**
     * Tests for changeLastName method.
     */
    @Test
    void testChangeLastNameStudentsMustBeForbidden() throws Exception {
        configureGateway(CHANGE_LAST_NAME_API);
        configureJwsMock(UserRole.STUDENT.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 19350204L)
                .put(LASTNAME, lastNameStudent).toString();

        mockMvcChangeLastName(json).andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testChangeLastNameNotFound() throws Exception {
        configureGateway(CHANGE_LAST_NAME_API);
        configureJwsMock(UserRole.ADMIN.name());

        String json = new ObjectMapper().createObjectNode().put(USERID, 4242442L)
            .put(LASTNAME, newLastName).toString();

        mockMvcChangeLastName(json).andExpect(status().isNotFound());
    }

    @Test
    void testChangeLastNameSuccessful() throws Exception {
        configureGateway(CHANGE_LAST_NAME_API);
        configureJwsMock(UserRole.ADMIN.name());

        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));
        final long userId = user.getUserId();
        String json = new ObjectMapper().createObjectNode().put(USERID, userId)
                .put(LASTNAME, newLastName).toString();

        mockMvcChangeLastName(json).andExpect(status().isOk());

        Optional<User> optionalUser = this.userRepository.findByUserId(userId);
        assert optionalUser.isPresent();
        Assertions.assertThat(optionalUser.get().getLastName()).isEqualTo(newLastName);

        this.userRepository.deleteById(userId);
    }


    /**
     * Tests for deleteByUserId method.
     */

    @Test
    void testDeleteByUserIdStudentsMustBeForbidden() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.STUDENT.name());

        mockMvcDeleteByUserId("123435").andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testDeleteByUserIdLecturersMustBeForbidden() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.LECTURER.name());

        mockMvcDeleteByUserId("123435").andExpect(status().isForbidden());
        assertRecordedRequestNull();
    }

    @Test
    void testDeleteByUserIdFailureAtAuth() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));
        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));

        MvcResult mvcResult =  mockMvcDeleteByUserId("" + user.getUserId()).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestWithJwt(recordedRequest, HttpMethod.DELETE);
    }

    @Test
    void testDeleteByUserIdSuccessful() throws Exception {
        configureGateway(DELETE_API);
        configureJwsMock(UserRole.ADMIN.name());
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        User user = userRepository.save(new User(username, firstNameStudent,
                lastNameStudent, UserRole.STUDENT));

        MvcResult mvcResult = mockMvcDeleteByUserId("" + user.getUserId()).andReturn();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertRecordedRequestWithJwt(recordedRequest, HttpMethod.DELETE);
    }


    /**
     * Tests for edge cases or exceptions.
     */

    @Test
    void testNotNumber() throws Exception {
        configureJwsMock(UserRole.ADMIN.name());
        mockMvcGetByUserId("nan").andExpect(status().isBadRequest());
    }

    @Test
    void testMissingValuesInJson() throws Exception {
        mockMvcRegister(createJson("ID", "4432894")).andExpect(status().isBadRequest());
    }


    @Test
    void testInvalidRole() throws Exception {
        configureJwsMock(UserRole.LECTURER.name());
        mockMvcGetByRole("MODERATOR").andExpect(status().isBadRequest());
    }


    /**
     * Remaining tests for 100% coverage.
     */

    @Test
    void testKindaSus() throws Exception {
        String message = mockMvc.perform(get("/api/users/admin"))
                .andExpect(status().isNotAcceptable())
            .andReturn().getResponse().getContentAsString();

        Assertions.assertThat(message).isEqualTo("Kinda sus, ngl!");
    }

    @Test
    void testGatewayConfigHost() {
        GatewayConfig config = new GatewayConfig();
        config.setHost("google.com");
        Assertions.assertThat(config.getHost()).isEqualTo("google.com");
    }

    @Test
    void testGatewayConfigPort() {
        GatewayConfig config = new GatewayConfig();
        config.setPort(8089);
        Assertions.assertThat(config.getPort()).isEqualTo(8089);
    }
}