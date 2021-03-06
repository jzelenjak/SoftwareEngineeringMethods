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
import nl.tudelft.sem.authentication.repositories.NotificationRepository;
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
    private transient NotificationRepository notificationRepository;

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
    void testGetNotificationsFromExistingUserAsAdminSuccess() throws Exception {
        Notification notification = new Notification(5695444L, "Hi Admin!");
        this.notificationRepository.save(notification);

        this.mockMvc
                .perform(get(GET_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5695444"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationRepository.findByUserId(5695444L);
        assert optionalList.isPresent();

        List<Notification> expectedList = new ArrayList<>();
        expectedList.add(notification);

        List<Notification> actualList = optionalList.get();
        Assertions.assertEquals(expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            Assertions.assertEquals(expectedList.get(i), actualList.get(i));
        }

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testGetNotificationsFromExistingUserAsStudentFailed() throws Exception {
        Notification notification = new Notification(5695444L, "Hi Admin!");
        this.notificationRepository.save(notification);

        this.mockMvc
                .perform(get(GET_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5695444"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testAddNotificationAsAdminSuccess() throws Exception {
        this.mockMvc
            .perform(post(ADD_URL)
                .contentType(APPLICATION_JSON)
                .content(createJson(USERID, "81395544414353", MESSAGE, "Hello World!"))
                    .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                .characterEncoding(UTF8))
            .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationRepository.findByUserId(81395544414353L);
        assert optionalList.isPresent();

        List<Notification> list = optionalList.get();
        Notification first = list.get(0);
        final long notificationId = first.getNotificationId();

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(notificationId);
        assert optionalNotification.isPresent();

        Notification notification = optionalNotification.get();
        Assertions.assertEquals(notification.getNotificationId(), notificationId);

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = LECTURERUSERNAME, password = SHAREDPASSWORD)
    void testAddNotificationAsLecturerSuccess() throws Exception {
        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "4441435386473932",
                                MESSAGE, "Hello class of 2021!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtLecturer)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalList =
                this.notificationRepository.findByUserId(4441435386473932L);
        assert optionalList.isPresent();

        List<Notification> list = optionalList.get();
        Notification first = list.get(0);
        final long notificationId = first.getNotificationId();

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(notificationId);
        assert optionalNotification.isPresent();

        Notification notification = optionalNotification.get();
        Assertions.assertEquals(notification.getNotificationId(), notificationId);

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testAddNotificationNoRightsFailed() throws Exception {
        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5555444", MESSAGE, "Hello World!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(1L);
        assert optionalNotification.isEmpty();

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testAddNotificationWithAnotherNotificationSuccess() throws Exception {
        Notification notification = new Notification(4864864486L, "Hello there!");
        this.notificationRepository.save(notification);

        this.mockMvc
                .perform(post(ADD_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "4864864486",
                                MESSAGE, "Hello Java!"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> optionalNotifications =
                this.notificationRepository.findByUserId(4864864486L);
        assert optionalNotifications.isPresent();
        List<Notification> list = optionalNotifications.get();
        Assertions.assertEquals(list.size(), 2);

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testChangeUserFromNotificationSuccess() throws Exception {
        Notification notification = new Notification(4648648L, "Hello!");
        this.notificationRepository.save(notification);
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
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals(1234567, newNotification.getUserId());

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testChangeUserFromNotificationAsStudentFailed() throws Exception {
        Notification notification = new Notification(4608648L, "Welkom!");
        this.notificationRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();

        this.mockMvc
                .perform(put(CHANGE_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId), NEWUSER, "754321"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals(4608648L, newNotification.getUserId());

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testChangeMessageFromNotificationSuccess() throws Exception {
        Notification notification = new Notification(4648648L, "Hi JavAa!");
        this.notificationRepository.save(notification);
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
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals("Hi Java!", newNotification.getMessage());

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testChangeMessageFromNotificationAsStudentFailed() throws Exception {
        Notification notification = new Notification(4648648L, "MT sucks");
        this.notificationRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();

        this.mockMvc
                .perform(put(CHANGE_MESSAGE_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId),
                                NEWMESSAGE, "MT is cool"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        Notification newNotification = optionalNotification.get();
        Assertions.assertEquals("MT sucks", newNotification.getMessage());

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testDeleteExistingNotificationById() throws Exception {
        Notification notification = new Notification(1212121L, "Delete me!");
        this.notificationRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();
        this.mockMvc
                .perform(delete(DELETE_BY_ID_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId)))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isEmpty();

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testDeleteExistingNotificationByIdAsStudentFailed() throws Exception {
        Notification notification = new Notification(1212121L, "I am sus");
        this.notificationRepository.save(notification);
        final long targetNotificationId = notification.getNotificationId();
        this.mockMvc
                .perform(delete(DELETE_BY_ID_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(NOTIFICATIONID,
                                String.valueOf(targetNotificationId)))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<Notification> optionalNotification =
                this.notificationRepository.findByNotificationId(targetNotificationId);
        assert optionalNotification.isPresent();

        this.notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = ADMINUSERNAME, password = SHAREDPASSWORD)
    void testDeleteExistingNotificationByUserId() throws Exception {
        Notification notification1 = new Notification(1212121L, "Delete me!");
        Notification notification2 = new Notification(1212121L, "Be gone!");
        Notification notification3 = new Notification(1212121L, "Lorem Ipsum!");

        List<Notification> list = new ArrayList<>();
        list.add(notification1);
        list.add(notification2);
        list.add(notification3);

        this.notificationRepository.save(notification1);
        this.notificationRepository.save(notification2);
        this.notificationRepository.save(notification3);

        this.mockMvc
                .perform(delete(DELETE_BY_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "1212121"))
                        .header(HttpHeaders.AUTHORIZATION, jwtAdmin)
                        .characterEncoding(UTF8))
                .andExpect(status().isOk());

        Optional<List<Notification>> listOfNotifications =
                this.notificationRepository.findByUserId(1212121L);
        assert listOfNotifications.isEmpty();
    }

    @Test
    @WithMockUser(username = STUDENTUSERNAME, password = SHAREDPASSWORD)
    void testDeleteExistingNotificationByUserIdAsStudentFailed() throws Exception {
        Notification notification1 = new Notification(5551234L, "Top");
        Notification notification2 = new Notification(5551234L, "Middle");
        Notification notification3 = new Notification(5551234L, "Bottom");

        List<Notification> list = new ArrayList<>();
        list.add(notification1);
        list.add(notification2);
        list.add(notification3);

        this.notificationRepository.save(notification1);
        this.notificationRepository.save(notification2);
        this.notificationRepository.save(notification3);

        this.mockMvc
                .perform(delete(DELETE_BY_USER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(createJson(USERID, "5551234"))
                        .header(HttpHeaders.AUTHORIZATION, jwtStudent)
                        .characterEncoding(UTF8))
                .andExpect(status().isForbidden());

        Optional<List<Notification>> listOfNotifications =
                this.notificationRepository.findByUserId(5551234L);
        assert listOfNotifications.isPresent();

        Assertions.assertEquals(list.size(), listOfNotifications.get().size());
    }
}