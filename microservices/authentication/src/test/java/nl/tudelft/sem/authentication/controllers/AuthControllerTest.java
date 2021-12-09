package nl.tudelft.sem.authentication.controllers;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Date;
import java.util.Optional;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
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
    private transient JwtTokenProvider jwtTokenProvider;

    @Autowired
    private transient UserDataRepository userDataRepository;

    private final transient ObjectMapper objectMapper = new ObjectMapper();

    private static final transient String USERNAME = "username";
    private static final transient String PASSWORD = "password";
    private static final transient String USERID = "userId";
    private static final transient String ROLE = "role";
    private static final transient String UTF8 = "utf-8";
    private static final transient String PREFIX = "Bearer ";

    private static final transient String LOGIN_URL = "/api/auth/login";
    private static final transient String REGISTER_URL = "/api/auth/register";
    private static final transient String CHANGE_PASSWORD_URL = "/api/auth/change_password";
    private static final transient String CHANGE_ROLE_URL = "/api/auth/change_role";
    private static final transient String DELETE_USER_URL = "/api/auth/delete";



    /**
     * A helper method to generate request body.
     *
     * @param args key-value pairs
     * @return  the JSON string with the specified key-value pairs
     */
    private String createJson(String... args) {
        ObjectNode node = objectMapper.createObjectNode();

        for (int i = 0; i < args.length; i += 2) {
            node.put(args[i], args[i + 1]);
        }
        return node.toString();
    }

    private String encode(String password) {
        return this.passwordEncoder.encode(password);
    }

    @Test
    @WithMockUser(username = "amongus", password = "kindasusngl")
    void registerSuccessfullyTest() throws Exception {
        this.mockMvc
            .perform(post(REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, "amongus",
                        PASSWORD, "kindasusngl", USERID, "2812040"))
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        this.userDataRepository.deleteById("amongus");
    }

    @Test
    @WithMockUser(username = "AMONGAS", password = "impostor")
    void registerUnsuccessfullyTest() throws Exception {
        String username = "AMONGAS";
        String password = "impostor";

        this.userDataRepository.save(new UserData(username,
                password, UserRole.LECTURER, 23014918L));

        this.mockMvc
            .perform(post(REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username,
                        PASSWORD, password, USERID, "1824910"))
                .characterEncoding(UTF8))
            .andExpect(status().isConflict());

        this.userDataRepository.deleteById(username);
    }


    @Test
    @WithMockUser(username = "admin1", password = "MyAdmin1")
    void changePasswordSuccessfullyTest() throws Exception {
        String username = "admin1";
        String password = "MyAdmin1";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.ADMIN, 2049100L));

        String jwt = jwtTokenProvider.createToken(2049100L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(put(CHANGE_PASSWORD_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username,
                        PASSWORD, password, "newPassword", "sssuss"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmUserButForgotToLogin", password = "StupidMe")
    void changePasswordUnsuccessfulTest() throws Exception {
        String username = "IAmUserButForgotToLogin";
        String password = "StupidMe";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.ADMIN, 8395620L));

        this.mockMvc
            .perform(put(CHANGE_PASSWORD_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username,
                        PASSWORD, password, "newPassword", "Dementia"))
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmInnocent", password = "HeHe")
    void changePasswordOtherThanMeTest() throws Exception {
        String username = "IAmInnocent";
        String password = "HeHe";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.ADMIN, 8164739L));

        String jwt = jwtTokenProvider.createToken(8164739L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String target = "IDontKnowHim";
        this.userDataRepository
                .save(new UserData(target, encode("BeepBoot"), UserRole.ADMIN, 9999887L));

        this.mockMvc
            .perform(put(CHANGE_PASSWORD_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, target,
                        PASSWORD, password, "new_password", "Dementia"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
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
            .save(new UserData(username, encode(password), UserRole.TA, 1048369L));
        String jwt = jwtTokenProvider.createToken(1048369L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(get(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username, PASSWORD, password))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Jws<Claims> claimsJws = this.jwtTokenProvider.validateAndParseToken(jwt);
        Assertions.assertNotNull(claimsJws);
        Assertions.assertEquals(1048369L,
                Long.parseLong(this.jwtTokenProvider.getSubject(claimsJws)));
        Assertions.assertEquals(this.jwtTokenProvider.getRole(claimsJws), UserRole.ADMIN.name());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "AAMOGUS", password = "sass")
    void loginNoUserTest() throws Exception {
        String username = "AAMOGUS";
        String password = "sass";

        this.mockMvc
            .perform(get(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username, PASSWORD, password))
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "AMMOGUS", password = "susngl")
    void loginBadCredentialsTest() throws Exception {
        String username = "AMMOGUS";
        String password = "susngl";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.STUDENT, 3912303L));

        this.mockMvc
            .perform(get(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username, PASSWORD, "!susngl"))
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmAdmin", password = "pwd8")
    void changeRoleSuccessfullyAsAdminTest() throws Exception {
        String username = "IAmAdmin";
        String password = "pwd8";
        String lecturerUsername = "IAmNotYetLecturer";
        String lecturerPassword = "SuperSad";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.ADMIN, 4295018L));
        String jwt = jwtTokenProvider.createToken(4295018L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.userDataRepository
            .save(new UserData(lecturerUsername, encode(lecturerPassword),
                    UserRole.STUDENT, 1122334L));


        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, lecturerUsername, ROLE, "lecturer"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Optional<UserData> admin = this.userDataRepository.findByUsername(username);
        assert admin.isPresent();
        Assertions.assertEquals(admin.get().getRole(), UserRole.ADMIN);

        Optional<UserData> lecturer = this.userDataRepository.findByUsername(lecturerUsername);
        assert lecturer.isPresent();
        Assertions.assertEquals(lecturer.get().getRole(), UserRole.LECTURER);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(lecturerUsername);
    }

    @Test
    @WithMockUser(username = "IAmGoodAdmin", password = "AdminPower")
    void changeRoleSuccessfullyAsAdminToStudentTest() throws Exception {
        String username = "IAmGoodAdmin";
        String password = "AdminPower";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.ADMIN, 4837291L));
        String jwt = jwtTokenProvider.createToken(4837291L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String retiredTa = "IAmRetiredTa";
        String taPassword = "SuperSad";

        this.userDataRepository
            .save(new UserData(retiredTa, encode(taPassword), UserRole.STUDENT, 2233445L));

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, retiredTa, ROLE, "student"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Optional<UserData> admin = this.userDataRepository.findByUsername(username);
        assert admin.isPresent();
        Assertions.assertEquals(admin.get().getRole(), UserRole.ADMIN);

        Optional<UserData> student = this.userDataRepository.findByUsername(retiredTa);
        assert student.isPresent();
        Assertions.assertEquals(student.get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(retiredTa);
    }

    @Test
    @WithMockUser(username = "IAmLecturer", password = "AllMightyMe")
    void changeRoleSuccessfullyAsLecturerTest() throws Exception {
        String username = "IAmLecturer";
        String password = "AllMightyMe";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.LECTURER, 39482394L));
        String jwt = jwtTokenProvider.createToken(39482394L, UserRole.LECTURER, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String ta = "IAmNotYetTa";
        String taPassword = "SoSadMe";

        this.userDataRepository
            .save(new UserData(ta, encode(taPassword), UserRole.STUDENT, 7654321L));

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, ta, ROLE, "ta"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Optional<UserData> lecturer = this.userDataRepository.findByUsername(username);
        assert lecturer.isPresent();
        Assertions.assertEquals(lecturer.get().getRole(), UserRole.LECTURER);

        Optional<UserData> foundTa = this.userDataRepository.findByUsername(ta);
        assert foundTa.isPresent();
        Assertions.assertEquals(foundTa.get().getRole(), UserRole.TA);

        this.userDataRepository.deleteById(username);
        this.userDataRepository.deleteById(ta);
    }

    @Test
    @WithMockUser(username = "IAmEvilLecturer", password = "Fraud")
    void changeIllegalRoleAsLecturerTest() throws Exception {
        String evilUsername = "IAmEvilLecturer";
        String evilPassword = "Fraud";

        this.userDataRepository
            .save(new UserData(evilUsername, encode(evilPassword), UserRole.LECTURER, 1017840L));
        String friendName = "IAmFriendOfEvilLecturer";
        String friendPassword = "SuperFraud";

        this.userDataRepository
            .save(new UserData(friendName, encode(friendPassword), UserRole.STUDENT, 4242421L));
        String jwt = jwtTokenProvider.createToken(1017840L, UserRole.LECTURER, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, friendName, ROLE, "admin"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        Optional<UserData> evil = this.userDataRepository.findByUsername(evilUsername);
        assert evil.isPresent();
        Assertions.assertEquals(evil.get().getRole(), UserRole.LECTURER);

        Optional<UserData> friend = this.userDataRepository.findByUsername(friendName);
        assert friend.isPresent();
        Assertions.assertEquals(friend.get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(evilUsername);
        this.userDataRepository.deleteById(friendName);
    }

    @Test
    @WithMockUser(username = "IAmNewLecturer", password = "NoFraudHere")
    void changeRoleIllegalTargetAsLecturerTest() throws Exception {
        String evilUsername = "IAmNewLecturer";
        String evilPassword = "NoFraudHere";

        this.userDataRepository
            .save(new UserData(evilUsername, encode(evilPassword), UserRole.LECTURER, 1000101L));
        String friendName = "IAmFriendOfLecturer";
        String friendPassword = "StillNoFraud";

        this.userDataRepository
            .save(new UserData(friendName, encode(friendPassword), UserRole.LECTURER, 6969691L));
        String jwt = jwtTokenProvider.createToken(1000101L, UserRole.LECTURER, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, friendName, ROLE, "TA"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        Optional<UserData> evil = this.userDataRepository.findByUsername(evilUsername);
        assert evil.isPresent();
        Assertions.assertEquals(evil.get().getRole(), UserRole.LECTURER);

        Optional<UserData> friend = this.userDataRepository.findByUsername(friendName);
        assert friend.isPresent();
        Assertions.assertEquals(friend.get().getRole(), UserRole.LECTURER);

        this.userDataRepository.deleteById(evilUsername);
        this.userDataRepository.deleteById(friendName);
    }

    @Test
    @WithMockUser(username = "IAmStudent", password = "pWord")
    void changeRoleNoRightsTest() throws Exception {
        String username = "IAmStudent";
        String password = "pWord";

        this.userDataRepository
            .save(new UserData(username, encode(password), UserRole.STUDENT, 1100983L));
        String jwt = jwtTokenProvider.createToken(1100983L, UserRole.STUDENT, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, username, ROLE, "TA"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        Optional<UserData> user = this.userDataRepository.findByUsername(username);
        assert user.isPresent();
        Assertions.assertEquals(user.get().getRole(), UserRole.STUDENT);

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmAdminButForgotToLogin", password = "pwdForgotToLogin")
    void changeRoleNoCredentialsTest() throws Exception {
        String adminName = "IAmAdminButForgotToLogin";
        String adminPassword = "pwdForgotToLogin";

        this.userDataRepository
            .save(new UserData(adminName, encode(adminPassword), UserRole.ADMIN, 61049405L));
        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, adminName, ROLE, "lecturer"))
                .header(HttpHeaders.AUTHORIZATION, PREFIX)
                .characterEncoding(UTF8))
            .andExpect(status().isForbidden());

        Optional<UserData> admin = this.userDataRepository.findByUsername(adminName);
        assert admin.isPresent();
        Assertions.assertEquals(admin.get().getRole(), UserRole.ADMIN);

        this.userDataRepository.deleteById(adminName);
    }

    @Test
    @WithMockUser(username = "IAmNew", password = "42")
    void changeNonExistingRole() throws Exception {
        String adminName = "IAmNew";
        String adminPassword = "42";

        this.userDataRepository
            .save(new UserData(adminName, encode(adminPassword), UserRole.ADMIN, 1048574L));
        String jwt = jwtTokenProvider.createToken(1048574L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
            .perform(put(CHANGE_ROLE_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERNAME, adminName, ROLE, "MODERATOR"))
                .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                .characterEncoding(UTF8))
            .andExpect(status().isBadRequest());

        Optional<UserData> admin = this.userDataRepository.findByUsername(adminName);
        assert admin.isPresent();
        Assertions.assertEquals(admin.get().getRole(), UserRole.ADMIN);

        this.userDataRepository.deleteById(adminName);
    }

    @Test
    @WithMockUser(username = "IAmAllMightyAdmin", password = "MeAllMightyMe")
    void deleteExistingUserTest() throws Exception {
        String username = "IAmAllMightyAdmin";
        String password = "MeAllMightyMe";

        this.userDataRepository
                .save(new UserData(username, encode(password), UserRole.ADMIN, 2948412L));
        String jwt = jwtTokenProvider.createToken(2948412L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        String studentName = "IAmCollegeDropout";
        String studentPassword = "MeSoSadMe";

        this.userDataRepository
                .save(new UserData(studentName, encode(studentPassword),
                        UserRole.STUDENT, 7654321L));

        this.mockMvc
                .perform(delete(DELETE_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERNAME, studentName))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        this.userDataRepository.deleteById(username);
    }

    @Test
    @WithMockUser(username = "IAmAllMightyAdmin2", password = "MeAllMighty")
    void deleteNonExistingUserTest() throws Exception {
        String username = "IAmAllMightyAdmin2";
        String password = "MeAllMighty";

        this.userDataRepository
                .save(new UserData(username, encode(password), UserRole.ADMIN, 2948412L));
        String jwt = jwtTokenProvider.createToken(2948412L, UserRole.ADMIN, new Date());
        String jwtPrefixed = PREFIX + jwt;

        this.mockMvc
                .perform(delete(DELETE_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERNAME, "BadStudent"))
                        .header(HttpHeaders.AUTHORIZATION, jwtPrefixed)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        this.userDataRepository.deleteById(username);
    }
}