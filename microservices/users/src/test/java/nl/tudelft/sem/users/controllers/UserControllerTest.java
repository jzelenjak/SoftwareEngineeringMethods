package nl.tudelft.sem.users.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.crypto.spec.SecretKeySpec;
import nl.tudelft.sem.users.config.GatewayConfig;
import nl.tudelft.sem.users.entities.User;
import nl.tudelft.sem.users.entities.UserRole;
import nl.tudelft.sem.users.services.UserService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    private final transient ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private transient MockMvc mockMvc;

    private static transient MockWebServer mockWebServer;

    @Value("${jwt.secretKeyString}")
    private transient String secretKeyString;

    private transient Key secretKey;

    @MockBean
    private transient GatewayConfig gatewayConfig;

    @MockBean
    private transient UserService userService;


    // Some constants for JSON fields
    private static final transient String USERID = "userId";
    private static final transient String USERNAME = "username";
    private static final transient String ROLE = "role";
    private static final transient String BEARER = "Bearer ";
    private static final transient String UTF8 = "uft-8";


    // Constants for APIs
    private static final transient String REGISTER_API = "/api/users/register";
    private static final transient String BY_USERNAME = "/api/users/by_username";
    private static final transient String BY_USER_ID_API = "/api/users/by_userid";
    private static final transient String BY_ROLE = "/api/users/by_role";
    private static final transient String CHANGE_ROLE_API = "/api/users/change_role";
    private static final transient String DELETE_API = "/api/users/delete";

    /**
     * Helper methods.
    */

    private String createJson(String... kvPairs) {
        ObjectNode node = mapper.createObjectNode();

        for (int i = 0; i < kvPairs.length; i += 2) {
            node.put(kvPairs[i], kvPairs[i + 1]);
        }
        return node.toString();
    }


    private String createToken(long userId, String role, Date date, long validityInMinutes) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put(ROLE, role);
        Date validity = new Date(date.getTime() + validityInMinutes * 60000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(date)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    void configureGateway(String path) {
        HttpUrl url = mockWebServer.url(path);
        Mockito.when(gatewayConfig.getHost()).thenReturn(url.host());
        Mockito.when(gatewayConfig.getPort()).thenReturn(url.port());
    }

    /**
     * Helper methods to mock userService and verify interactions with it.
     * Used to reduce code duplication in the test class
     */

    private void mockRegister(String username, String firstName, String lastName, long toReturn) {
        if (toReturn == -1) {
            Mockito.when(userService.registerUser(username, firstName, lastName))
                .thenThrow(DataIntegrityViolationException.class);
        } else {
            Mockito.when(userService.registerUser(username, firstName, lastName))
                .thenReturn(toReturn);
        }
    }

    private void verifyRegister(String username, String firstName, String lastName, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .registerUser(username, firstName, lastName);
    }

    private void mockGetByUsername(String netId, User userToReturn) {
        if (userToReturn == null) {
            Mockito.when(userService.getUserByNetId(netId))
                .thenReturn(Optional.empty());
        } else {
            Mockito.when(userService.getUserByNetId(netId))
                .thenReturn(Optional.of(userToReturn));
        }
    }

    private void verifyGetByUsername(String netId, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .getUserByNetId(netId);
    }

    private void mockGetByUserId(long userId, User userToReturn) {
        if (userToReturn == null) {
            Mockito.when(userService.getUserByUserId(userId))
                .thenReturn(Optional.empty());
        } else {
            Mockito.when(userService.getUserByUserId(userId))
                .thenReturn(Optional.of(userToReturn));
        }
    }

    private void verifyGetByUserId(long userId, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .getUserByUserId(userId);
    }

    private void mockGetByRole(UserRole role, List<User> usersToReturn) {
        Mockito.when(userService.getUsersByRole(role))
            .thenReturn(usersToReturn);
    }

    private void verifyGetByRole(UserRole role, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .getUsersByRole(role);
    }

    private void mockChangeRole(long userId, UserRole newRole, UserRole requester, boolean result) {
        Mockito.when(userService.changeRole(userId, newRole, requester))
                .thenReturn(result);
    }

    private void verifyChangeRole(long userId, UserRole newRole, UserRole requester, int times) {
        if (userId == -1) {
            Mockito.verify(userService, Mockito.times(times))
                    .changeRole(anyLong(), any(), any());
        } else {
            Mockito
                .verify(userService, Mockito.times(times))
                .changeRole(userId, newRole, requester);
        }
    }

    private void mockIsAllowedToChangeRole(long userId, UserRole newRole,
                                           UserRole requester, boolean allowed) {
        Mockito.when(userService.isAllowedToChangeRole(userId, newRole, requester))
                .thenReturn(allowed);
    }

    private void verifyIsAllowedToChangeRole(long userId, UserRole newRole,
                                             UserRole requester, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .isAllowedToChangeRole(userId, newRole, requester);
    }

    private void mockDeleteByUserId(long userId, UserRole requesterRole, boolean result) {
        Mockito.when(userService.deleteUserByUserId(userId, requesterRole))
            .thenReturn(result);
    }

    private void verifyDeleteByUserId(long userId, UserRole requester, int times) {
        if (userId == -1) {
            Mockito
                    .verify(userService, Mockito.times(times))
                    .deleteUserByUserId(Mockito.anyLong(), any());
        } else {
            Mockito
                    .verify(userService, Mockito.times(times))
                    .deleteUserByUserId(userId, requester);
        }
    }

    private void mockIsAllowedToDelete(UserRole requesterRole, boolean result) {
        Mockito.when(userService.isAllowedToDelete(requesterRole))
                .thenReturn(result);
    }

    private void verifyIsAllowedToDelete(UserRole requester, int times) {
        Mockito
            .verify(userService, Mockito.times(times))
            .isAllowedToDelete(requester);
    }

    /**
     * Helper methods for configuring MockMVC.
     */

    private ResultActions mockMvcRegister(String path, String json) throws Exception {
        return mockMvc
                .perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .characterEncoding(UTF8));
    }

    private ResultActions mockMvcGetByUsername(String path, String json) throws Exception {
        return mockMvc
                .perform(get(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .characterEncoding(UTF8));
    }

    private ResultActions mockMvcGetByUserId(String path, String json) throws Exception {
        return mockMvc
                .perform(get(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .characterEncoding(UTF8));
    }

    private ResultActions mockMvcGetByRole(String path, String json) throws Exception {
        return mockMvc
                .perform(get(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .characterEncoding(UTF8));
    }

    private ResultActions mockMvcChangeRole(String path, String json,
                                            String token) throws Exception {
        return mockMvc
                .perform(put(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .characterEncoding(UTF8));
    }

    private ResultActions mockMvcDeleteByUserId(String path, String json,
                                                String token) throws Exception {
        return mockMvc
                .perform(delete(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .characterEncoding(UTF8));
    }

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setup() {
        this.secretKey = new SecretKeySpec(secretKeyString.getBytes(),
                            SignatureAlgorithm.HS256.getJcaName());
    }

    @Test
    void dummy() {
        System.out.println(this.secretKeyString);
    }

    /**
     * Tests for registerUser method.
     */

    @Test
    void registerUserAlreadyExistsLocallyTest() throws Exception {
        String uname = "S.Bar@student.tudelft.nl";
        String fname = "Sasha";
        String lname = "Bar";
        String pass = "123";
        configureGateway("/api/auth/register");

        // Configure userService mock
        mockRegister(uname, fname, lname, -1);

        // Assert
        mockMvcRegister(REGISTER_API, createJson(USERNAME, uname, "firstName",
                    fname, "lastName", lname, "password", pass))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        // Extra checks
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
            .assertThat(recordedRequest)
            .isNull();

        // Verify mocks
        verifyRegister(uname, fname, lname, 1);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
    }

    @Test
    void registerUserAlreadyExistsOnlyInAuthTest() throws Exception {
        // Prepare and mock the objects
        String uname = "S.Bar@student.tudelft.nl";
        String fname = "Sasha";
        String lname = "Bar";

        configureGateway("/api/auth/register");
        mockRegister(uname, fname, lname, 5623546L);
        mockDeleteByUserId(5623546L, UserRole.ADMIN, true);
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(409));


        // Assert
        MvcResult mvcRes = mockMvcRegister(REGISTER_API, createJson(USERNAME, uname, "firstName",
                                    fname, "lastName", lname, "password", "123"))
                                .andReturn();
        mockMvc.perform(asyncDispatch(mvcRes))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("POST");


        // Verify mocks
        verifyRegister(uname, fname, lname, 1);
        verifyDeleteByUserId(5623546L, UserRole.ADMIN, 1);
    }

    @Test
    void registerUserSuccessTest() throws Exception {
        // Prepare and mock the objects
        String uname = "S.Bar@student.tudelft.nl";
        String fname = "Sasha";
        String lname = "Bar";
        final String expectedJson = new ObjectMapper().createObjectNode()
                                    .put(USERID, 3443546).toString();

        configureGateway("/api/auth/register");
        mockRegister(uname, fname, lname, 3443546L);
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(200));


        // Assert
        MvcResult mvcRes = mockMvcRegister(REGISTER_API, createJson(USERNAME, uname,
                "firstName", fname, "lastName", lname, "password", "1234"))
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcRes))
                    .andExpect(status().isOk())
                .andExpect(content().string(expectedJson));

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("POST");


        // Verify mocks
        verifyRegister(uname, fname, lname, 1);
        verifyDeleteByUserId(3443546L, UserRole.ADMIN, 0);
    }


    /**
     * Tests for getByUsername method.
     */


    @Test
    void getByUsernameNotFoundTest() throws Exception {
        String uname = "B.Bob@student.tudelft.nl";

        mockGetByUsername(uname, null);
        mockMvcGetByUsername(BY_USERNAME, createJson(USERNAME, uname))
                .andExpect(status().isNotFound());

        verifyGetByUsername(uname, 1);
    }

    @Test
    void getByUsernameFoundTest() throws Exception {
        String uname = "B.Bob@student.tudelft.nl";
        User user = new User(uname, "Boob", "Bob", UserRole.STUDENT);
        user.setUserId(5545365L);

        mockGetByUsername(uname, user);
        String res = mockMvcGetByUsername(BY_USERNAME,
                    createJson(USERNAME, uname))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User received = mapper.readValue(res, User.class);

        Assertions
                .assertThat(received)
                .isEqualTo(user);

        verifyGetByUsername(uname, 1);
    }


    /**
     * Tests for getByUserId method.
     */


    @Test
    void getByUserIdNotFoundTest() throws Exception {
        long userId = 4536894L;

        mockGetByUserId(userId, null);
        mockMvcGetByUserId(BY_USER_ID_API, createJson(USERID, String.valueOf(userId)))
                .andExpect(status().isNotFound());

        verifyGetByUserId(userId, 1);
    }

    @Test
    void getByUserIdFoundTest() throws Exception {
        long userId = 4536894L;
        User user = new User("rrandom@tudelft.nl", "rand", "random", UserRole.LECTURER);
        user.setUserId(userId);

        mockGetByUserId(userId, user);
        String res = mockMvcGetByUserId(BY_USER_ID_API,
                        createJson(USERID, String.valueOf(userId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User received = mapper.readValue(res, User.class);

        Assertions
                .assertThat(received)
                .isEqualTo(user);

        verifyGetByUserId(userId, 1);
    }


    /**
     * Tests for getByRole method.
     */


    @Test
    void getByRoleNotFoundTest() throws Exception {
        List<User> users = new ArrayList<>();

        mockGetByRole(UserRole.CANDIDATE_TA, users);

        mockMvcGetByRole(BY_ROLE, createJson(ROLE, UserRole.CANDIDATE_TA.name()))
                .andExpect(status().isNotFound());

        verifyGetByRole(UserRole.CANDIDATE_TA, 1);
    }

    @Test
    void getByRoleFoundTest() throws Exception {
        List<User> users = List.of(
                new User("abcdef@student.tudelft.nl", "abc", "def", UserRole.STUDENT),
                new User("defghi@student.tudelft.nl", "def", "ghi", UserRole.STUDENT),
                new User("ghijkl@student.tudelft.nl", "ghi", "jkl", UserRole.STUDENT)
        );
        users.get(0).setUserId(4389775L);
        users.get(1).setUserId(3485664L);
        users.get(2).setUserId(2365449L);

        mockGetByRole(UserRole.STUDENT, users);

        String res = mockMvcGetByRole(BY_ROLE, createJson(ROLE,
                UserRole.STUDENT.name()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> received = mapper.readValue(res, new TypeReference<List<User>>(){});

        Assertions
                .assertThat(received)
                .isEqualTo(users);

        verifyGetByRole(UserRole.STUDENT, 1);
    }


    /**
     * Tests for changeRole method.
     */


    @Test
    void changeRoleInvalidTokenTest() throws Exception {
        // Generate the JWT token
        String token = createToken(3456445L,
                UserRole.STUDENT.name(), new Date(), 15);
        String badToken = "oupps" + token;
        String prefixedBadToken = BEARER + badToken;

        // Prepare
        configureGateway(CHANGE_ROLE_API);


        // Assert
        mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 5422341L)
                        .put(ROLE, UserRole.TA.name())
                        .toString(), prefixedBadToken)
                .andExpect(status().isUnauthorized());
        Assertions
                .assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS))
                .isNull();


        // Verify mocks
        verifyChangeRole(-1, UserRole.TA, UserRole.STUDENT, 0);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
    }

    @Test
    void changeRoleTokenDoesNotStartWithBearerTest() throws Exception {
        // Generate the JWT token
        String token = createToken(3456445L,
                UserRole.STUDENT.name(), new Date(), 15);
        String prefixedToken = "Prefix " + token;

        // Prepare
        configureGateway(CHANGE_ROLE_API);


        // Assert
        mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 3456774L)
                        .put(ROLE, UserRole.TA.name())
                        .toString(), prefixedToken)
                .andExpect(status().isBadRequest());
        Assertions
                .assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS))
                .isNull();


        // Verify mocks
        verifyChangeRole(-1, UserRole.TA, UserRole.STUDENT, 0);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
    }

    @Test
    void changeRoleExpiredTest() throws Exception {
        // Generate the JWT token
        String token = createToken(5456445L,
                UserRole.ADMIN.name(), new Date(), 0);

        // Prepare
        configureGateway(CHANGE_ROLE_API);


        // Assert
        mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 4536654L)
                        .put(ROLE, UserRole.TA.name())
                        .toString(), BEARER + token)
                .andExpect(status().isUnauthorized());
        Assertions
                .assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS))
                .isNull();


        // Verify mocks
        verifyChangeRole(-1, UserRole.TA, UserRole.ADMIN, 0);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
    }

    @Test
    void changeRoleUnauthorizedTest() throws Exception {
        // Generate the JWT token
        final String token = createToken(5456445L,
                UserRole.TA.name(), new Date(), 3000);

        // Prepare and mock the objects
        configureGateway(CHANGE_ROLE_API);
        mockIsAllowedToChangeRole(2376889L, UserRole.TA, UserRole.TA, false);


        // Assert
        mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 2376889L)
                        .put(ROLE, UserRole.TA.name())
                        .toString(), BEARER + token)
                .andExpect(status().isUnauthorized());
        Assertions
                .assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS))
                .isNull();


        // Verify mocks
        verifyIsAllowedToChangeRole(2376889L, UserRole.TA, UserRole.TA, 1);
        verifyChangeRole(-1, UserRole.TA, UserRole.TA, 0);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
    }

    @Test
    void changeRoleFailureAtAuthTest() throws Exception {
        // Generate the JWT token
        final String token = createToken(3756849L,
                UserRole.ADMIN.name(), new Date(), 20);

        // Prepare and mock the objects
        User userFromRepo = new User("sus@tudelft.nl", "suss",
                "sass", UserRole.STUDENT);
        userFromRepo.setUserId(5465321L);

        configureGateway(CHANGE_ROLE_API);
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(403));
        mockIsAllowedToChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, true);
        mockGetByUserId(5465321L, userFromRepo);


        // Assert
        MvcResult mvcResult = mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 5465321L)
                        .put(ROLE, UserRole.LECTURER.name())
                        .toString(), BEARER + token)
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isForbidden());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("PUT");

        // Verify mocks
        verifyGetByUserId(5465321L, 1);
        verifyChangeRole(-1, UserRole.LECTURER, UserRole.ADMIN, 0);
        verifyIsAllowedToChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, 1);
    }

    @Test
    void changeRoleSuccessfulTest() throws Exception {
        // Generate the JWT token
        final String token = createToken(3756849L,
                UserRole.ADMIN.name(), new Date(), 20);

        // Prepare
        User userFromRepo = new User("sus@tudelft.nl", "suss",
                "sass", UserRole.STUDENT);
        userFromRepo.setUserId(5465321L);

        configureGateway(CHANGE_ROLE_API);
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(200));
        mockChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, true);
        mockGetByUserId(5465321L, userFromRepo);
        mockIsAllowedToChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, true);

        // Assert
        MvcResult mvcResult = mockMvcChangeRole(CHANGE_ROLE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, 5465321L)
                        .put(ROLE, UserRole.LECTURER.name())
                        .toString(), BEARER + token)
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("PUT");

        // Verify mocks
        verifyChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, 1);
        verifyIsAllowedToChangeRole(5465321L, UserRole.LECTURER, UserRole.ADMIN, 1);
    }


    /**
     * Tests for deleteByUserId method.
     */


    @Test
    void deleteByUserIdUnauthorizedTest() throws Exception {
        long userId = 5768009L;
        User user = new User("rrandom@tudelft.nl", "rand",
                "random", UserRole.STUDENT);
        user.setUserId(userId);

        // Generate the JWT token
        final String token = createToken(3006445L,
                UserRole.STUDENT.name(), new Date(), 25);

        // Prepare and mock the objects
        mockIsAllowedToDelete(UserRole.STUDENT, false);
        configureGateway("/api/auth/delete");


        // Assert
        mockMvcDeleteByUserId(DELETE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, userId)
                        .toString(), BEARER + token)
                .andExpect(status().isUnauthorized());

        Assertions
                .assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS))
                .isNull();


        // Verify mocks
        verifyDeleteByUserId(-1, UserRole.STUDENT, 0);
        verifyIsAllowedToDelete(UserRole.STUDENT, 1);
    }

    @Test
    void deleteByUserIdFailureAtAuthTest() throws Exception {
        long userId = 4323556L;
        User user = new User("rrr@tudelft.nl", "r", "rr", UserRole.STUDENT);
        user.setUserId(userId);

        // Generate the JWT token
        final String token = createToken(6545332L,
                UserRole.ADMIN.name(), new Date(), 20);

        // Prepare and mock the objects
        mockIsAllowedToDelete(UserRole.ADMIN, true);
        mockGetByUserId(userId, user);
        configureGateway("/api/auth/delete");
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(403));


        // Assert
        MvcResult mvcResult = mockMvcDeleteByUserId(DELETE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, userId)
                        .toString(), BEARER + token)
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isForbidden());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");


        // Verify mocks
        verifyIsAllowedToDelete(UserRole.ADMIN, 1);
        verifyDeleteByUserId(-1, UserRole.ADMIN, 0);
        verifyGetByUserId(userId, 1);
    }

    @Test
    void deleteByUserIdSuccessfulTest() throws Exception {
        long userId = 2345345L;
        User user = new User("rrr@tudelft.nl", "r", "rr", UserRole.STUDENT);
        user.setUserId(userId);

        // Generate the JWT token
        final String token = createToken(6545332L,
                UserRole.ADMIN.name(), new Date(), 20);

        // Prepare and mock the objects
        mockIsAllowedToDelete(UserRole.ADMIN, true);
        mockDeleteByUserId(userId, UserRole.ADMIN, true);
        mockGetByUserId(userId, user);
        configureGateway("/api/auth/delete");
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(200));


        // Assert
        MvcResult mvcResult = mockMvcDeleteByUserId(DELETE_API,
                new ObjectMapper().createObjectNode()
                        .put(USERID, userId)
                        .toString(), BEARER + token)
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        Assertions
                .assertThat(recordedRequest)
                .isNotNull();
        Assertions
                .assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");


        // Verify mocks
        verifyIsAllowedToDelete(UserRole.ADMIN, 1);
        verifyDeleteByUserId(userId, UserRole.ADMIN, 1);
        verifyGetByUserId(userId, 1);
    }


    /**
     * Tests for edge cases or exceptions.
     */


    @Test
    void missingValuesInJsonTest() throws Exception {
        mockMvcGetByUserId(BY_USER_ID_API, createJson("ID", String.valueOf(4432894L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corruptedJsonTest() throws Exception {
        mockMvcGetByUserId(BY_USER_ID_API, "hehehe")
                .andExpect(status().isBadRequest());
    }

    @Test
    void notNumberTest() throws Exception {
        mockMvcGetByUserId(BY_USER_ID_API, createJson("userId", "nan"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Remaining tests for 100% coverage.
     */
    @Test
    void gatewayConfigHostTest() {
        GatewayConfig config = new GatewayConfig();
        config.setHost("google.com");
        Assertions
                .assertThat(config.getHost())
                .isEqualTo("google.com");
    }

    @Test
    void gatewayConfigPortTest() {
        GatewayConfig config = new GatewayConfig();
        config.setPort(8089);
        Assertions
                .assertThat(config.getPort())
                .isEqualTo(8089);
    }

    @Test
    void invalidRoleTest() throws Exception {
        mockMvcGetByRole(BY_ROLE, createJson(ROLE, "MODERATOR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidJwsRole() throws Exception {
        // Generate the JWT token
        String token = createToken(2543432L,
                "MODERATOR", new Date(), 10);
        String prefixedToken = BEARER + token;

        // Assert
        mockMvcChangeRole(CHANGE_ROLE_API,
                createJson(USERID, String.valueOf(34126654L), ROLE,
                        UserRole.TA.name()), prefixedToken)
                .andExpect(status().isBadRequest());

        // Verify mocks
        verifyChangeRole(-1, UserRole.TA, UserRole.ADMIN, 0);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

}