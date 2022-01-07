package nl.tudelft.sem.courses.integrationtests;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.controllers.CourseController;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.TeachesRepository;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CourseControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    private static final String createCoursePath = "/api/courses/create";
    private static final String getCoursePath = "/api/courses/get/courses/";
    private static final String courseCode = "CSE2216";
    private static final String createGradePath = "/api/courses/create/grade";
    private static final String assignLecturerPath = "/api/courses/assign/lecturer/1/1";
    private static final ZonedDateTime date = ZonedDateTime.now();
    private static final CourseRequest courseRequest = new CourseRequest(courseCode,
            date, date, 1);

    @Autowired
    private transient CourseRepository courseRepository;

    @Autowired
    private transient ObjectMapper objectMapper;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> claimsJwsMock;

    @Autowired
    private transient CourseController courseController;

    @BeforeEach
    void setup() {
        //mock the jwtUtils
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claimsJwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn("ADMIN");
        when(jwtUtils.getUserId(Mockito.any())).thenReturn((long) 1);
    }

    @Test
    void testCourseCreationWithNoConflictingCourses() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testCourseCreationWithConflictingCourses() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCourseCreationWithNoAuthorization() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isForbidden());

    }

    @Test
    void testGettingCoursesByCode() throws Exception {
        //First we add the course
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //now we check if we can get the list of courses by course code.
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content, new TypeReference<>() {
        });
        //Expected Course Response
        Assertions.assertNotNull(courses);
        Assertions.assertNotNull(courses.get(0));
        Assertions.assertEquals(1, courses.size());
        Assertions.assertEquals(courseCode, courses.get(0).getCourseCode());
    }

    //testing for null or empty courses.
    @Test
    void testGetCoursesByCodeEmptyCoursesListReturned() throws Exception {
        mockMvc.perform(get(getCoursePath + courseCode)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCourseByCodeUnauthorized() throws Exception {
        mockMvc.perform(get(getCoursePath + courseCode))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCoursesByIdsStudent() throws Exception {
        // Create course objects used for testing (fetch from DB to avoid time rounding conflicts)
        Course course1 = courseRepository.save(new Course(courseCode, date, date, 42));
        Course course2 = courseRepository.save(new Course(courseCode, date, date, 1337));
        Course course3 = courseRepository.save(new Course(courseCode, date, date, 486));
        course1 = courseRepository.findById(course1.getId()).orElseGet(Course::new);
        course2 = courseRepository.findById(course2.getId()).orElseGet(Course::new);
        course3 = courseRepository.findById(course3.getId()).orElseGet(Course::new);
        Assertions.assertNotEquals(course1.getId(), course2.getId());
        Assertions.assertNotEquals(course1.getId(), course3.getId());
        Assertions.assertNotEquals(course2.getId(), course3.getId());

        // Mock the role of the user
        when(jwtUtils.getRole(Mockito.any())).thenReturn("STUDENT");

        // Compose JSON object for the request
        List<Long> courseIds = List.of(course1.getId(), course3.getId());
        String json = objectMapper.createObjectNode().set("courseIds",
                objectMapper.valueToTree(courseIds)).toString();

        // Expected response
        ObjectNode expected = objectMapper.createObjectNode();
        expected.set(String.valueOf(course1.getId()),
                objectMapper.valueToTree(new CourseResponse(course1)));
        expected.set(String.valueOf(course3.getId()),
                objectMapper.valueToTree(new CourseResponse(course3)));

        // Initiate the request
        mockMvc.perform(post("/api/courses/get-multiple")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(expected.toString(), false));
    }

    @Test
    void testGetCoursesByIdsLecturer() throws Exception {
        // Create course objects used for testing (fetch from DB to avoid time rounding conflicts)
        Course course1 = courseRepository.save(new Course(courseCode, date, date, 42));
        Course course2 = courseRepository.save(new Course(courseCode, date, date, 1337));
        Course course3 = courseRepository.save(new Course(courseCode, date, date, 486));
        course1 = courseRepository.findById(course1.getId()).orElseGet(Course::new);
        course2 = courseRepository.findById(course2.getId()).orElseGet(Course::new);
        course3 = courseRepository.findById(course3.getId()).orElseGet(Course::new);
        Assertions.assertNotEquals(course1.getId(), course2.getId());
        Assertions.assertNotEquals(course1.getId(), course3.getId());
        Assertions.assertNotEquals(course2.getId(), course3.getId());

        // Mock the role of the user
        when(jwtUtils.getRole(Mockito.any())).thenReturn("LECTURER");

        // Compose JSON object for the request
        List<Long> courseIds = List.of(course1.getId(), course3.getId());
        String json = objectMapper.createObjectNode().set("courseIds",
                objectMapper.valueToTree(courseIds)).toString();

        // Expected response
        ObjectNode expected = objectMapper.createObjectNode();
        expected.set(String.valueOf(course1.getId()),
                objectMapper.valueToTree(new CourseResponse(course1)));
        expected.set(String.valueOf(course3.getId()),
                objectMapper.valueToTree(new CourseResponse(course3)));

        // Initiate the request
        mockMvc.perform(post("/api/courses/get-multiple")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(expected.toString(), false));
    }

    @Test
    void testGetCoursesByIdsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/courses/get-multiple")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCourseById() throws Exception {
        //First we add the course
        MvcResult mvcResult1 = mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String content1 = mvcResult1.getResponse().getContentAsString();
        Course course = objectMapper.readValue(content1, Course.class);
        //now we check if it has the correct course id.
        MvcResult mvcResult = mockMvc.perform(get("/api/courses/get/" + course.getId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        CourseResponse response = objectMapper.readValue(content, CourseResponse.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(courseRequest.getCourseCode(), response.getCourseCode());
    }

    @Test
    void testGetCourseByIdWithCourseWhichDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/courses/get/1")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCourseByIdUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/courses/get/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteAnExistingCourse() throws Exception {
        //First we add the course
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content, new TypeReference<>() {
        });

        //now we delete the course
        mockMvc.perform(delete("/api/courses/delete/" + courses.get(0).getCourseId())
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteNonExistentCourse() throws Exception {
        mockMvc.perform(delete("/api/courses/delete/1")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCourseUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/courses/delete/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddGrade() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(
                        get(getCoursePath + courseCode)
                                .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content, new TypeReference<>() {
        });
        long courseId = courses.get(0).getCourseId();
        //now we begin the grade id's test

        GradeRequest gradeRequest = new GradeRequest(courseId, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testAddGradeWhenGradeAlreadyExists() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content, new TypeReference<>() {
        });
        long courseId = courses.get(0).getCourseId();
        //now we begin the grade id's test

        GradeRequest gradeRequest = new GradeRequest(courseId, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post(createGradePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddGradeUnauthorized() throws Exception {
        GradeRequest gradeRequest = new GradeRequest(1, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetGradeOfUser() throws Exception {
        mockMvc.perform(post(createCoursePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content, new TypeReference<>() {
        });
        long courseId = courses.get(0).getCourseId();
        //now we begin the grade id's test
        GradeRequest gradeRequest = new GradeRequest(courseId, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

        //adds the grade. Now we get the grade.
        MvcResult mvcResult1 = mockMvc.perform(get("/api/courses/get/grade/1/" + courseId)
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();
        String content2 = mvcResult1.getResponse().getContentAsString();
        float grade = Float.parseFloat(content2);

        Assertions.assertEquals(5.75f, grade, 0.1f);
    }

    @Test
    void testGetGradeOfUserNoGradeFound() throws Exception {
        mockMvc.perform(get("/api/courses/get/grade/1/1")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetGradeOfUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses/get/grade/1/1"))
                .andExpect(status().isForbidden());
    }

    //testing the isAuthorizedMethod
    @Test
    void testIsAuthorizedMethodNoToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn(null);

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assertions.assertNull(result);
    }

    //testing the isAuthorizedMethod
    @Test
    void testIsAuthorizedNoJwsClaims() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("Valid");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(null);

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assertions.assertNull(result);
    }

    //testing the isAuthorizedMethod
    @Test
    void testIsAuthorizedNotCorrectRole() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("Valid");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claimsJwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn("STUDENT");

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assertions.assertEquals("STUDENT", jwtUtils.getRole(result));
        Assertions.assertTrue(courseController.checkIfStudent(result));
        Assertions.assertNotEquals("LECTURER", jwtUtils.getRole(result));
        Assertions.assertFalse(courseController.checkIfLecturerOrAdmin(result));
    }

    @Test
    void testAddNewLecturerToCourse() throws Exception {
        mockMvc.perform(post(assignLecturerPath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void testAddNewLecturerToCourseUnauthorized() throws Exception {
        mockMvc.perform(post(assignLecturerPath))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCoursesOfLecturer() throws Exception {
        mockMvc.perform(post(assignLecturerPath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/assign/lecturer/1/2")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/assign/lecturer/1/3")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        MvcResult mvcResult = mockMvc.perform(get("/api/courses/get/lecturer/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isOk())
                .andReturn();

        List<Long> expectedResult = Arrays.asList(1L, 2L, 3L);

        String response = mvcResult.getResponse().getContentAsString();
        List<Long> result = objectMapper.readValue(response, new TypeReference<>() {
        });

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void testGetCoursesOfLecturerFailedToGetAnyCourses() throws Exception {
        mockMvc.perform(get("/api/courses/get/lecturer/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCoursesOfLecturerUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses/get/lecturer/courses/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDoesLecturerTeachCourse() throws Exception {
        mockMvc.perform(post(assignLecturerPath)
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/courses/get/teaches/1/1")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void testDoesLecturerTeachCourseDoesNotTeachCourse() throws Exception {
        mockMvc.perform(get("/api/courses/get/teaches/1/1")
                        .header(HttpHeaders.AUTHORIZATION, "")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDoesLecturerTeachCourseUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses/get/teaches/1/1"))
                .andExpect(status().isForbidden());
    }
}
