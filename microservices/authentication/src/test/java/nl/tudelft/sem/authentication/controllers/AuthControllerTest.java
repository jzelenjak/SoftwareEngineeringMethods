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
    private final transient String loginUrl = "/api/auth/login";
    private final transient String registerUrl = "/api/auth/register";
    private final transient String changePasswordUrl = "/api/auth/change_password";
    private final transient String changeRoleUrl = "/api/auth/change_role";
    private final transient String usernameStr = "username";
    private final transient String passwordStr = "password";
    private final transient String roleStr = "role";
    private final transient String utf8Str = "utf-8";
    private final transient String prefix = "Bearer ";


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
            .perform(post(registerUrl)
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
            .perform(post(registerUrl)
                    .contentType(APPLICATION_JSON)
                    .content(createJson(usernameStr, username, passwordStr, password))
                    .characterEncoding(utf8Str))
            .andExpect(status().isConflict());

        this.userDataRepository.deleteById(username);
    }


    @Test
    @WithMockUser(username = "admin1", password = "MyAdmin1")
    void changePasswordSuccessfullyTest() throws Exception {
        String username = "admin1";
        String password = "MyAdmin1";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        String jwt = jwtUtils.createToken(username, UserRole.ADMIN, new Date());
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
            .perform(put(changePasswordUrl)
                    .contentType(APPLICATION_JSON)
                    .content(createJson(usernameStr, username,
                            passwordStr, password, "new_password", "sssuss"))
                    .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                    .characterEncoding(utf8Str))
                .andExpect(status().isOk());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmUserButForgotToLogin", password = "StupidMe")
    void changePasswordUnsuccessfulTest() throws Exception {
        String username = "IAmUserButForgotToLogin";
        String password = "StupidMe";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        this.mockMvc
                .perform(put(changePasswordUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, username,
                                passwordStr, password, "new_password", "Dementia"))
                        .characterEncoding(utf8Str))
                .andExpect(status().isUnauthorized());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmInnocent", password = "HeHe")
    void changePasswordOtherThanMeTest() throws Exception {
        String username = "IAmInnocent";
        String password = "HeHe";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        String jwt = jwtUtils.createToken(username, UserRole.ADMIN, new Date());
        String jwtPrefixed = prefix + jwt;
        String target = "IDontKnowHim";
        this.userDataRepository
                .save(new UserData(target,
                        this.passwordEncoder.encode("BeepBoot"), UserRole.ADMIN));
        this.mockMvc
                .perform(put(changePasswordUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, target,
                                passwordStr, password, "new_password", "Dementia"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(target);
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
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
                .perform(get(loginUrl)
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
            .perform(get(loginUrl)
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
                        this.passwordEncoder.encode(password), UserRole.STUDENT));

        this.mockMvc
            .perform(get(loginUrl)
                .contentType(APPLICATION_JSON)
                .content(createJson(usernameStr, username, passwordStr, "!susngl"))
                .characterEncoding(utf8Str))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmAdmin", password = "pwd8")
    void changeRoleSuccessfullyAsAdminTest() throws Exception {
        String username = "IAmAdmin";
        String password = "pwd8";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        String jwt = jwtUtils.createToken(username, UserRole.ADMIN, new Date());
        String jwtPrefixed = prefix + jwt;
        String lecturer = "IAmNotYetLecturer";
        String lecturerPassword = "SuperSad";
        this.userDataRepository
                .save(new UserData(lecturer,
                        this.passwordEncoder.encode(lecturerPassword), UserRole.STUDENT));
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, lecturer, roleStr, "lecturer"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isOk());
        Assertions.assertEquals(this.userDataRepository.findByUsername(username)
                .get().getRole(), UserRole.ADMIN);
        Assertions.assertEquals(this.userDataRepository.findByUsername(lecturer)
                .get().getRole(), UserRole.LECTURER);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(lecturer);
    }

    @Test
    @WithMockUser(username = "IAmGoodAdmin", password = "AdminPower")
    void changeRoleSuccessfullyAsAdminToStudentTest() throws Exception {
        String username = "IAmGoodAdmin";
        String password = "AdminPower";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.ADMIN));
        String jwt = jwtUtils.createToken(username, UserRole.ADMIN, new Date());
        String jwtPrefixed = prefix + jwt;
        String retiredTa = "IAmRetiredTa";
        String taPassword = "SuperSad";
        this.userDataRepository
                .save(new UserData(retiredTa,
                        this.passwordEncoder.encode(taPassword), UserRole.STUDENT));
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, retiredTa, roleStr, "student"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isOk());
        Assertions.assertEquals(this.userDataRepository.findByUsername(username)
                .get().getRole(), UserRole.ADMIN);
        Assertions.assertEquals(this.userDataRepository.findByUsername(retiredTa)
                .get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(retiredTa);
    }

    @Test
    @WithMockUser(username = "IAmLecturer", password = "AllMightyMe")
    void changeRoleSuccessfullyAsLecturerTest() throws Exception {
        String username = "IAmLecturer";
        String password = "AllMightyMe";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.LECTURER));
        String jwt = jwtUtils.createToken(username, UserRole.LECTURER, new Date());
        String jwtPrefixed = prefix + jwt;
        String ta = "IAmNotYetTa";
        String taPassword = "SoSadMe";
        this.userDataRepository
                .save(new UserData(ta,
                        this.passwordEncoder.encode(taPassword), UserRole.STUDENT));
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, ta, roleStr, "ta"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isOk());
        Assertions.assertEquals(this.userDataRepository.findByUsername(username)
                .get().getRole(), UserRole.LECTURER);
        Assertions.assertEquals(this.userDataRepository.findByUsername(ta)
                .get().getRole(), UserRole.TA);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(ta);
    }

    @Test
    @WithMockUser(username = "IAmEvilLecturer", password = "Fraud")
    void changeIllegalRoleAsLecturerTest() throws Exception {
        String evilUsername = "IAmEvilLecturer";
        String evilPassword = "Fraud";
        this.userDataRepository
                .save(new UserData(evilUsername,
                        this.passwordEncoder.encode(evilPassword), UserRole.LECTURER));
        String friendName = "IAmFriendOfEvilLecturer";
        String friendPassword = "SuperFraud";
        this.userDataRepository
                .save(new UserData(friendName,
                        this.passwordEncoder.encode(friendPassword), UserRole.STUDENT));
        String jwt = jwtUtils.createToken(evilUsername, UserRole.LECTURER, new Date());
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, friendName, roleStr, "admin"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isForbidden());
        Assertions.assertEquals(this.userDataRepository.findByUsername(evilUsername)
                .get().getRole(), UserRole.LECTURER);
        Assertions.assertEquals(this.userDataRepository.findByUsername(friendName)
                .get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(evilUsername);
        this.userDataRepository.deleteById(friendName);
    }

    @Test
    @WithMockUser(username = "IAmNewLecturer", password = "NoFraudHere")
    void changeRoleIllegalTargetAsLecturerTest() throws Exception {
        String evilUsername = "IAmNewLecturer";
        String evilPassword = "NoFraudHere";
        this.userDataRepository
                .save(new UserData(evilUsername,
                        this.passwordEncoder.encode(evilPassword), UserRole.LECTURER));
        String friendName = "IAmFriendOfLecturer";
        String friendPassword = "StillNoFraud";
        this.userDataRepository
                .save(new UserData(friendName,
                        this.passwordEncoder.encode(friendPassword), UserRole.LECTURER));
        String jwt = jwtUtils.createToken(evilUsername, UserRole.LECTURER, new Date());
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, friendName, roleStr, "TA"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isForbidden());
        Assertions.assertEquals(this.userDataRepository.findByUsername(evilUsername)
                .get().getRole(), UserRole.LECTURER);
        Assertions.assertEquals(this.userDataRepository.findByUsername(friendName)
                .get().getRole(), UserRole.LECTURER);

        this.userDataRepository.deleteById(evilUsername);
        this.userDataRepository.deleteById(friendName);
    }

    @Test
    @WithMockUser(username = "IAmStudent", password = "pWord")
    void changeRoleNoRightsTest() throws Exception {
        String username = "IAmStudent";
        String password = "pWord";
        this.userDataRepository
                .save(new UserData(username,
                        this.passwordEncoder.encode(password), UserRole.STUDENT));
        String jwt = jwtUtils.createToken(username, UserRole.STUDENT, new Date());
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, username, roleStr, "TA"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isForbidden());
        Assertions.assertEquals(this.userDataRepository.findByUsername(username)
                .get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmAdminButForgotToLogin", password = "pwdForgotToLogin")
    void changeRoleNoCredentialsTest() throws Exception {
        String adminName = "IAmAdminButForgotToLogin";
        String adminPassword = "pwdForgotToLogin";
        this.userDataRepository
                .save(new UserData(adminName,
                        this.passwordEncoder.encode(adminPassword), UserRole.ADMIN));
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, adminName, roleStr, "lecturer"))
                        .header(HttpHeaders.AUTHORIZATION, prefix)
                        .characterEncoding(utf8Str))
                .andExpect(status().isForbidden());
        Assertions.assertEquals(this.userDataRepository.findByUsername(adminName)
                .get().getRole(), UserRole.ADMIN);

        this.userDataRepository.deleteById(adminName);
    }

    @Test
    @WithMockUser(username = "IAmNew", password = "42")
    void changeNonExistingRole() throws Exception {
        String adminName = "IAmNew";
        String adminPassword = "42";
        this.userDataRepository
                .save(new UserData(adminName,
                        this.passwordEncoder.encode(adminPassword), UserRole.ADMIN));
        String jwt = jwtUtils.createToken(adminName, UserRole.ADMIN, new Date());
        String jwtPrefixed = prefix + jwt;
        this.mockMvc
                .perform(put(changeRoleUrl)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(usernameStr, adminName, roleStr, "MODERATOR"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(utf8Str))
                .andExpect(status().isBadRequest());
        Assertions.assertEquals(this.userDataRepository.findByUsername(adminName)
                .get().getRole(), UserRole.ADMIN);

        this.userDataRepository.deleteById(adminName);
    }


}