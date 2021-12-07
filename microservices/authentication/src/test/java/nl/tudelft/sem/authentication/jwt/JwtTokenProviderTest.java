package nl.tudelft.sem.authentication.jwt;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.security.Key;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import nl.tudelft.sem.authentication.service.AuthService;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class JwtTokenProviderTest {
    @Autowired
    @Qualifier("secretKey")
    private transient Key hmacKey;

    @Autowired
    private transient JwtUtils jwtUtils;

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient UserDataRepository userDataRepository;

    @Autowired
    private transient AuthService authService;

    private transient JwtTokenProvider jwtTokenProvider;

    private final transient ObjectMapper objectMapper = new ObjectMapper();

    private static final transient String PREFIX = "Bearer ";
    private static final transient String UTF8 = "utf-8";
    private static final transient String USERNAME = "username";
    private static final transient String PASSWORD = "password";
    private static final transient String VALIDITY_IN_MINUTES = "validityInMinutes";
    private static final transient String LOGIN_API_PATH = "/api/auth/login";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(authService, hmacKey, jwtUtils);
        ReflectionTestUtils.setField(jwtTokenProvider, VALIDITY_IN_MINUTES, 10);
    }

    /**
     * A helper method to generate request body.
     *
     * @param args key-value pairs (the number must be even)
     * @return  the JSON string with the specified key value pairs
     */
    private String createJson(String... args) {
        ObjectNode node = objectMapper.createObjectNode();

        for (int i = 0; i < args.length; i += 2) {
            node.put(args[i], args[i + 1]);
        }
        return node.toString();
    }


    @Test
    @WithMockUser(username = "amogus", password = "NoFraudAllowed")
    void createAndResolveValidTokenTest() throws Exception {
        String jwt = jwtTokenProvider.createToken(1738290L, UserRole.STUDENT, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String username = "admin";
        String password = "NoFraudAllowed";

        this.userDataRepository.save(new UserData(username, password, UserRole.STUDENT, 1738290L));

        HttpServletRequest request =
                this.mockMvc
                        .perform(get(LOGIN_API_PATH)
                            .contentType(APPLICATION_JSON)
                            .content(createJson(USERNAME, username, PASSWORD, password))
                            .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                            .characterEncoding(UTF8))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtTokenProvider.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith(PREFIX),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertTrue(jwtTokenProvider.validateToken(tokenBody),
                "Invalid or expired token");
        Assertions.assertEquals(1738290L, Long.parseLong(jwtTokenProvider.getSubject(tokenBody)),
                "Decoded subject does not match the original one");
        Assertions.assertEquals(UserRole.STUDENT.name(), jwtTokenProvider.getRole(tokenBody),
                "Decoded role does not match the original one");

        this.userDataRepository.deleteById(username);
    }



    @Test
    @WithMockUser(username = "admin1", password = "NoFraudAllowed1")
    void createAndResolveInValidTokenExpiredTest() throws Exception {
        Date date = new Date(new Date().getTime() - 10 * 60000);
        String jwt = jwtTokenProvider.createToken(9577681L, UserRole.ADMIN, date);
        String jwtPrefixed = PREFIX + jwt;

        String username = "admin1";
        String password = "NoFraudAllowed1";

        this.userDataRepository.save(new UserData(username, password, UserRole.ADMIN, 9577681L));

        HttpServletRequest request =
                this.mockMvc
                        .perform(get(LOGIN_API_PATH)
                                .contentType(APPLICATION_JSON)
                                .content(createJson(USERNAME, username, PASSWORD, password))
                                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                                .characterEncoding(UTF8))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtTokenProvider.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith(PREFIX),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertNull(jwtUtils.validateAndParseClaims(tokenBody),
                "The token must be invalid or expired");

        this.userDataRepository.deleteById(username);
    }

    @Test
    void createAndResolveInValidTokenZeroValidityTest() throws Exception {
        ReflectionTestUtils.setField(jwtTokenProvider, VALIDITY_IN_MINUTES, 0);

        String jwt = jwtTokenProvider.createToken(9048182L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String username = "admin2";
        String password = "NoFraudAllowed2";

        this.userDataRepository.save(new UserData(username, password, UserRole.ADMIN, 9048182L));

        HttpServletRequest request =
                this.mockMvc
                        .perform(get(LOGIN_API_PATH)
                                .contentType(APPLICATION_JSON)
                                .content(createJson(USERNAME, username, PASSWORD, password))
                                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                                .characterEncoding(UTF8))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtTokenProvider.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertNull(jwtUtils.validateAndParseClaims(tokenBody),
                "The token must be invalid or expired");

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "admin3", password = "NoFraudAllowed3")
    void resolveTokenNotStartsWithBearerTest() throws Exception {
        String jwt = jwtTokenProvider.createToken(1047399L, UserRole.ADMIN, new Date());
        String username = "admin3";
        String password = "NoFraudAllowed3";

        HttpServletRequest request =
                this.mockMvc
                        .perform(get(LOGIN_API_PATH)
                                .contentType(APPLICATION_JSON)
                                .content(createJson(USERNAME, username, PASSWORD, password))
                                .header(HttpHeaders.AUTHORIZATION, jwt)
                                .characterEncoding(UTF8))
                        .andReturn()
                        .getRequest();
        Assertions.assertNull(jwtTokenProvider.getAuthentication("a.b.c"));
        Assertions.assertNull(jwtTokenProvider.resolveToken(request));
    }
}