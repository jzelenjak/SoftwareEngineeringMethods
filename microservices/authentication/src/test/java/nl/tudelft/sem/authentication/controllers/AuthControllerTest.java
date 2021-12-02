package nl.tudelft.sem.authentication.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Date;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtUtils;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient PasswordEncoder passwordEncoder;

    @Autowired
    private transient JwtUtils jwtUtils;

    @Autowired
    private transient UserDataRepository userDataRepository;

    private final transient ObjectMapper objectMapper = new ObjectMapper();

    private final transient String url = "/api/auth/%s";

    private final transient String usernameStr = "username";

    private final transient String passwordStr = "password";

    private final transient String utf8Str = "utf-8";

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
    @WithMockUser(username = "amongus", password = "kindasusngl")
    void registerSuccessfullyTest() throws Exception {
        this.mockMvc
            .perform(post(String.format(url, "register"))
                .contentType(APPLICATION_JSON)
                .content(createJson(usernameStr, "amongus", passwordStr, "kindasusngl"))
                .characterEncoding(utf8Str))
            .andExpect(status().isOk());

        this.userDataRepository.deleteById("amongus");
    }

    @Test
    @WithMockUser(username = "AMONGAS", password = "impostor")
    void registerUnsuccessfullyTest() throws Exception {
        String username = "AMONGAS";
        String password = "impostor";
        this.userDataRepository.save(new UserData(username, password, UserRole.LECTURER));

        this.mockMvc
            .perform(post(String.format(url, "register"))
                    .contentType(APPLICATION_JSON)
                    .content(createJson(usernameStr, username, passwordStr, password))
                    .characterEncoding(utf8Str))
            .andExpect(status().isConflict());

        this.userDataRepository.deleteById(username);
    }


    @Test
    @WithMockUser(username = "admin1", password = "suuuuuus")
    void changePasswordSuccessfullyTest() throws Exception {
        String username = "admin1";
        String password = "suuuuuus";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        String jwt = jwtUtils.createToken("admin1", UserRole.ADMIN, new Date());
        String jwtPrefixed = "Bearer " + jwt;
        this.mockMvc
            .perform(put(String.format(url, "change_password"))
                    .contentType(APPLICATION_JSON)
                    .content(createJson(usernameStr, username,
                            passwordStr, password, "new_password", "sssuss"))
                    .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                    .characterEncoding(utf8Str))
                .andExpect(status().isOk());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "admin2", password = "amooooogus")
    void loginSuccessfullyTest() throws Exception {
        String username = "admin2";
        String password = "amooooogus";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.TA));
        String jwt = jwtUtils.createToken("admin2", UserRole.ADMIN, new Date());
        String jwtPrefixed = "Bearer " + jwt;
        this.mockMvc
                .perform(get(String.format(url, "login"))
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, username, passwordStr, password))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                        .andExpect(status().isOk());

        Assertions.assertTrue(this.jwtUtils.validateToken(jwt));
        Assertions.assertEquals(this.jwtUtils.getUsername(jwt), username);
        Assertions.assertEquals(this.jwtUtils.getRole(jwt), UserRole.ADMIN.name());


        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "AAMOGUS", password = "sass")
    void loginNoUserTest() throws Exception {
        String username = "AAMOGUS";
        String password = "sass";
        this.mockMvc
            .perform(get(String.format(url, "login"))
                .contentType(APPLICATION_JSON)
                .content(createJson(usernameStr, username, passwordStr, password))
                .characterEncoding(utf8Str))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "AMMOGUS", password = "susngl")
    void loginBadCredentialsTest() throws Exception {
        String username = "AMMOGUS";
        String password = "susngl";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));

        this.mockMvc
            .perform(get(String.format(url, "login"))
                .contentType(APPLICATION_JSON)
                .content(createJson(usernameStr, username, passwordStr, "!susngl"))
                .characterEncoding(utf8Str))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
    }
}