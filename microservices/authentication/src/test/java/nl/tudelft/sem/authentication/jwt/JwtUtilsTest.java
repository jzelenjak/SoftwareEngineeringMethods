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
class JwtUtilsTest {
    @Autowired
    @Qualifier("secretKey")
    private transient Key hmacKey;

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient UserDataRepository userDataRepository;

    private transient JwtUtils jwtUtils;

    private final transient String prefix = "Bearer ";

    private final transient ObjectMapper objectMapper = new ObjectMapper();

    private transient String utf;

    private transient String usernameStr = "username";

    private transient String passwordStr = "password";


    @BeforeEach
    void setUp() {
        utf = "utf-8";
        jwtUtils = new JwtUtils(hmacKey);
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 10);
    }

    /**
     * A helper method to generate request body.
     *
     * @param args key-value pairs
     * @return  the JSON string with the specified key-values
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
        Date date = new Date();
        String jwt = jwtUtils.createToken("admin", UserRole.STUDENT, date);
        String jwtPrefixed = prefix + jwt;

        String username = "admin";
        String password = "NoFraudAllowed";
        this.userDataRepository.save(new UserData(username, password, UserRole.STUDENT));
        HttpServletRequest request =
                this.mockMvc
                        .perform(get("/api/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(createJson("username", username, passwordStr, password))
                                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                                .characterEncoding(utf))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtUtils.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith(prefix),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertTrue(jwtUtils.validateToken(tokenBody),
                "Invalid or expired token");
        Assertions.assertEquals("admin", jwtUtils.getUsername(tokenBody),
                "Decoded username does not match the original one");
        Assertions.assertEquals(UserRole.STUDENT.name(), jwtUtils.getRole(tokenBody),
                "Decoded role does not match the original one");
        this.userDataRepository.deleteById(username);
    }



    @Test
    @WithMockUser(username = "admin1", password = "NoFraudAllowed1")
    void createAndResolveInValidTokenExpiredTest() throws Exception {

        Date date = new Date(new Date().getTime() - 10 * 60000);
        String jwt = jwtUtils.createToken("admin1", UserRole.ADMIN, date);
        String jwtPrefixed = prefix + jwt;

        String username = "admin1";
        String password = "NoFraudAllowed1";
        this.userDataRepository.save(new UserData(username, password, UserRole.ADMIN));
        HttpServletRequest request =
                this.mockMvc
                        .perform(get("/api/auth/" + "login")
                                .contentType(APPLICATION_JSON)
                                .content(createJson(usernameStr, username, "password", password))
                                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                                .characterEncoding(utf))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtUtils.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith(prefix),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
        this.userDataRepository.deleteById(username);
    }

    @Test
    void createAndResolveInValidTokenZeroValidityTest() throws Exception {
        ReflectionTestUtils.setField(jwtUtils, "validityInMinutes", 0);

        Date date = new Date();
        String jwt = jwtUtils.createToken("admin2", UserRole.ADMIN, date);
        String jwtPrefixed = prefix + jwt;

        String username = "admin2";
        String password = "NoFraudAllowed2";
        this.userDataRepository.save(new UserData(username, password, UserRole.ADMIN));
        HttpServletRequest request =
                this.mockMvc
                        .perform(get("/api/auth" + "/login")
                                .contentType(APPLICATION_JSON)
                                .content(createJson(usernameStr, username, passwordStr, password))
                                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                                .characterEncoding(utf))
                        .andReturn()
                        .getRequest();

        String tokenBody = jwtUtils.resolveToken(request);

        Assertions.assertFalse(tokenBody.startsWith("Bearer "),
                "The resolved token must not start with the prefix 'Bearer '");
        Assertions.assertFalse(jwtUtils.validateToken(tokenBody),
                "The token must be invalid or expired");
        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "admin3", password = "NoFraudAllowed3")
    void resolveTokenNotStartsWithBearerTest() throws Exception {
        String jwt = jwtUtils.createToken("admin3", UserRole.ADMIN, new Date());
        String username = "admin3";
        String password = "NoFraudAllowed3";
        HttpServletRequest request =
                this.mockMvc
                        .perform(get("/api/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(createJson(usernameStr, username, passwordStr, password))
                                .header(HttpHeaders.AUTHORIZATION, jwt)
                                .characterEncoding(utf))
                        .andReturn()
                        .getRequest();
        Assertions.assertNull(jwtUtils.getAuthentication("a.b.c"));
        Assertions.assertNull(jwtUtils.resolveToken(request));
    }
}