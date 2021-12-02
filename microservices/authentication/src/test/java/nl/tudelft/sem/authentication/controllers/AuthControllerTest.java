package nl.tudelft.sem.authentication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtUtils;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    /**
     * A helper method to generate request body
     * @param args key-value pairs
     * @return  the JSON string with the specified key-values
     */
    private String createJSON(String... args) {
        ObjectNode node = objectMapper.createObjectNode();

        for (int i = 0; i < args.length; i += 2) {
            node.put(args[i], args[i+1]);
        }
        return node.toString();
    }


    @Test
    @WithMockUser(username = "amongus", password = "kindasusngl")
    void registerSuccessfullyTest() throws Exception {
        this.mockMvc
            .perform(post(String.format(url, "register"))
                .contentType(APPLICATION_JSON)
                .content(createJSON("username","amongus", "password", "kindasusngl"))
                .characterEncoding("utf-8"))
            .andExpect(status().isOk());

        this.userDataRepository.deleteById("amongus");
    }

    @Test
    @WithMockUser(username = "AMONGAS", password = "impostor")
    void registerUnsuccessfullyTest() throws Exception {
        this.userDataRepository.save(new UserData("AMONGAS", "impostor", UserRole.LECTURER));

        this.mockMvc
            .perform(post(String.format(url, "register"))
                    .contentType(APPLICATION_JSON)
                    .content(createJSON("username","AMONGAS", "password", "impostor"))
                    .characterEncoding("utf-8"))
            .andExpect(status().isConflict());

        this.userDataRepository.deleteById("AMONGAS");
    }


    @Test
    @WithMockUser(username = "AMOOOGUS", password = "suuuuuus")
    void changePasswordSuccessfullyTest() throws Exception {
        this.userDataRepository.save(new UserData("AMOOOGUS",
                                        this.passwordEncoder.encode("suuuuuus"), UserRole.STUDENT));

        this.mockMvc
            .perform(put(String.format(url, "change_password"))
                    .contentType(APPLICATION_JSON)
                    .content(createJSON("username","AMOOOGUS","password", "suuuuuus", "new_password", "sssuss"))
                    .characterEncoding("utf-8"))
                .andExpect(status().isOk());

        this.userDataRepository.deleteById("AMOOOGUS");
    }

    @Test
    @WithMockUser(username = "GUS", password = "sas")
    void changePasswordNoUserTest() throws Exception {
        this.mockMvc
            .perform(put(String.format(url,"change_password"))
                .contentType(APPLICATION_JSON)
                .content(createJSON("username","GUS", "password", "sas", "new_password", "sssasss"))
                .characterEncoding("utf-8"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "AMOGUSAMOGUS", password = "nglngl")
    void changePasswordBadCredentialsTest() throws Exception {
        this.userDataRepository.save(new UserData("AMOGUSAMOGUS",
                                        this.passwordEncoder.encode("nglngl"), UserRole.STUDENT));

        this.mockMvc
            .perform(put(String.format(url,"change_password"))
                .contentType(APPLICATION_JSON)
                .content(createJSON("username","AMOGUSAMOGUS", "password", "!nglngl", "new_password", "sssasss"))
                .characterEncoding("utf-8"))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById("AMOGUSAMOGUS");
    }


    @Test
    @WithMockUser(username = "AMGUS", password = "amooooogus")
    void loginSuccessfullyTest() throws Exception {
        this.userDataRepository.save(new UserData("AMGUS",
                                        this.passwordEncoder.encode("amooooogus"), UserRole.TA));

        String jwt =
                this.mockMvc
                    .perform(get(String.format(url, "login"))
                        .contentType(APPLICATION_JSON)
                        .content(createJSON("username", "AMGUS", "password", "amooooogus"))
                        .characterEncoding("utf-8"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getHeader("Authorization");

        jwt = this.jwtUtils.resolveToken(jwt);
        assertTrue(this.jwtUtils.validateToken(jwt));
        assertEquals(this.jwtUtils.getUsername(jwt), "AMGUS");
        assertEquals(this.jwtUtils.getRole(jwt), UserRole.TA.name());


        this.userDataRepository.deleteById("AMGUS");
    }

    @Test
    @WithMockUser(username = "AAMOGUS", password = "sass")
    void loginNoUserTest() throws Exception {
        this.mockMvc
            .perform(get(String.format(url,"login"))
                .contentType(APPLICATION_JSON)
                .content(createJSON("username", "AAMOGUS", "password", "sass"))
                .characterEncoding("utf-8"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "AMMOGUS", password = "susngl")
    void loginBadCredentialsTest() throws Exception {
        this.userDataRepository.save(new UserData("AMMOGUS",
                                        this.passwordEncoder.encode("susngl"), UserRole.ADMIN));

        this.mockMvc
            .perform(get(String.format(url,"login"))
                .contentType(APPLICATION_JSON)
                .content(createJSON("username", "AMMOGUS", "password", "!susngl"))
                .characterEncoding("utf-8"))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById("AMMOGUS");
    }
}