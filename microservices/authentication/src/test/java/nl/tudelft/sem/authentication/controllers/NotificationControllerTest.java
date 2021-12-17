package nl.tudelft.sem.authentication.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.authentication.entities.Notification;
import nl.tudelft.sem.authentication.entities.UserData;
import nl.tudelft.sem.authentication.jwt.JwtTokenProvider;
import nl.tudelft.sem.authentication.repositories.NotificationDataRepository;
import nl.tudelft.sem.authentication.repositories.UserDataRepository;
import nl.tudelft.sem.authentication.security.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient NotificationDataRepository notificationDataRepository;

    @Autowired
    private transient UserDataRepository userDataRepository;

    @Autowired
    private transient JwtTokenProvider jwtTokenProvider;


    private final transient ObjectMapper objectMapper = new ObjectMapper();

    // Some fixed values we use often.
    private static final transient String NOTIFICATIONID = "notificationId";
    private static final transient String USERID = "userId";
    private static final transient String MESSAGE = "message";
    private static final transient String NEWUSER = "newUser";
    private static final transient String NEWMESSAGE = "newMessage";
    private static final transient String UTF8 = "utf-8";
    private static final transient String PREFIX = "Bearer ";

    private static final transient String ADMINUSERNAME = "IAmAllMightyAdmin ";
    private static final transient String LECTURERUSERNAME = "IAmBestLecturer";
    private static final transient String STUDENTUSERNAME = "IAmSimpleStudent";
    private static final transient String SHAREDPASSWORD = "ThisIsEncrypted ";
    private static final transient long ADMINID = 4864861L;
    private static final transient long LECTURERID = 9864869L;
    private static final transient long STUDENTID = 8536291L;

    // Some values we initialize before each test.
    private static transient String jwtAdmin;
    private static transient String jwtLecturer;
    private static transient String jwtStudent;

    private static final transient String GET_URL = "/api/auth/notifications/get";
    private static final transient String ADD_URL = "/api/auth/notifications/add";
    private static final transient String CHANGE_USER_URL = "/api/auth/notifications/change_user";
    private static final transient String CHANGE_MESSAGE_URL
            = "/api/auth/notifications/change_message";
    private static final transient String DELETE_BY_ID_URL = "/api/auth/notifications/delete";
    private static final transient String DELETE_BY_USER_URL
            = "/api/auth/notifications/delete_user";



    /**
     * A helper method to generate request body.
     *
     * @param args key-value pairs.
     * @return  the JSON string with the specified key-value pairs.
     */
    private String createJson(String... args) {
        ObjectNode node = objectMapper.createObjectNode();
        for (int i = 0; i < args.length; i += 2) {
            node.put(args[i], args[i + 1]);
        }
        return node.toString();
    }

    @BeforeEach
    void setupBefore() {
        jwtAdmin = PREFIX + jwtTokenProvider.createToken(ADMINID, UserRole.ADMIN, new Date());
        jwtLecturer = PREFIX + jwtTokenProvider.createToken(LECTURERID,
                UserRole.LECTURER, new Date());
        jwtStudent = PREFIX + jwtTokenProvider.createToken(STUDENTID,
                UserRole.STUDENT, new Date());
        UserData admin = new UserData(ADMINUSERNAME, SHAREDPASSWORD, UserRole.ADMIN, ADMINID);
        UserData lecturer = new UserData(LECTURERUSERNAME, SHAREDPASSWORD,
                UserRole.LECTURER, LECTURERID);
        UserData student = new UserData(STUDENTUSERNAME, SHAREDPASSWORD,
                UserRole.STUDENT, STUDENTID);
        this.userDataRepository.save(admin);
        this.userDataRepository.save(student);
        this.userDataRepository.save(lecturer);
    }

    @AfterEach
    void setupAfter() {
        this.userDataRepository.deleteById(ADMINUSERNAME);
        this.userDataRepository.deleteById(STUDENTUSERNAME);
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void getNotificationsFromExistingUserAsAdminSuccessTest() throws Exception {
        Notification notification = new Notification(5695444L, "Hi Admin!");
        this.notificationDataRepository.save(notification);

        this.mockMvc
                .perform(get(GET_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5695444"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationDataRepository.findByUserId(5695444L);
        assert optionalList.isPresent();

        List<Notification> expectedList = new ArrayList<>();
        expectedList.add(notification);

        List<Notification> actualList = optionalList.get();
        Assertions.assertEquals(expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            Assertions.assertEquals(expectedList.get(i), actualList.get(i));
        }

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void getNotificationsFromExistingUserAsStudentFailedTest() throws Exception {
        Notification notification = new Notification(5695444L, "Hi Admin!");
        this.notificationDataRepository.save(notification);

        this.mockMvc
                .perform(get(GET_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5695444"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void addNotificationAsAdminSuccessTest() throws Exception {
        this.mockMvc
            .perform(post(ADD_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERID, "81395544414353", MESSAGE, "Hello World!"))
                    .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationDataRepository.findByUserId(81395544414353L);
        assert optionalList.isPresent();

        List<Notification> list = optionalList.get();
        Notification first = list.get(0);
        final long notificationId = first.getNotificationId();

        Optional<Notification> optionalNotification =
                this.notificationDataRepository.findByNotificationId(notificationId);
        assert optionalNotification.isPresent();

        Notification notification = optionalNotification.get();
        Assertions.assertEquals(notification.getNotificationId(), notificationId);

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = LECTURERUSERNAME, password = SHAREDPASSWORD)
    void addNotificationAsLecturerSuccessTest() throws Exception {
        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "4441435386473932",
                                MESSAGE, "Hello class of 2021!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtLecturer)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationDataRepository.findByUserId(4441435386473932L);
        assert optionalList.isPresent();

        List<Notification> list = optionalList.get();
        Notification first = list.get(0);
        final long notificationId = first.getNotificationId();

        Optional<Notification> optionalNotification =
                this.notificationDataRepository.findByNotificationId(notificationId);
        assert optionalNotification.isPresent();

        Notification notification = optionalNotification.get();
        Assertions.assertEquals(notification.getNotificationId(), notificationId);

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void addNotificationNoRightsFailedTest() throws Exception {
        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5555444", MESSAGE, "Hello World!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<Notification> optionalNotification =
                this.notificationDataRepository.findByNotificationId(1L);
        assert optionalNotification.isEmpty();

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void addNotificationWithAnotherNotificationSuccessTest() throws Exception {
        Notification notification = new Notification(4864864486L, "Hello there!");
        this.notificationDataRepository.save(notification);

        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "4864864486",
                                MESSAGE, "Hello Java!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalNotifications =
                this.notificationDataRepository.findByUserId(4864864486L);
        assert optionalNotifications.isPresent();
        List<Notification> list = optionalNotifications.get();
        Assertions.assertEquals(list.size(), 2);

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void changeUserFromNotificationSuccessTest() throws Exception {
        Notification notification = new Notification(4648648L, "Hello!");
        this.notificationDataRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();

        this.mockMvc
                .perform(put(CHANGE_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId), NEWUSER, "1234567"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<Notification> optionalNotification =
                this.notificationDataRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals(1234567, newNotification.getUserId());

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void changeMessageFromNotificationSuccessTest() throws Exception {
        Notification notification = new Notification(4648648L, "Hi JavAa!");
        this.notificationDataRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();

        this.mockMvc
                .perform(put(CHANGE_MESSAGE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId),
                                NEWMESSAGE, "Hi Java!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<Notification> optionalNotification =
                this.notificationDataRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals("Hi Java!", newNotification.getMessage());

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void deleteExistingNotificationByIdTest() throws Exception {
        Notification notification = new Notification(1212121L, "Delete me!");
        this.notificationDataRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();
        this.mockMvc
                .perform(delete(DELETE_BY_ID_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId)))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        this.notificationDataRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void deleteExistingNotificationByUserIdTest() throws Exception {
        Notification notification1 = new Notification(1212121L, "Delete me!");
        Notification notification2 = new Notification(1212121L, "Be gone!");
        Notification notification3 = new Notification(1212121L, "Lorem Ipsum!");

        List<Notification> list = new ArrayList<>();
        list.add(notification1);
        list.add(notification2);
        list.add(notification3);

        this.notificationDataRepository.save(notification1);
        this.notificationDataRepository.save(notification2);
        this.notificationDataRepository.save(notification3);

        this.mockMvc
                .perform(delete(DELETE_BY_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "1212121"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> listOfNotifications =
                this.notificationDataRepository.findByUserId(1212121L);
        assert listOfNotifications.isEmpty();
    }


}