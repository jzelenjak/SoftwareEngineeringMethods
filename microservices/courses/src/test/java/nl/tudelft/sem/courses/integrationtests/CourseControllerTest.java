package nl.tudelft.sem.courses.integrationtests;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.controllers.CourseController;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.Assert;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class CourseControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    private static final String contentType = "Content-Type";
    private static final String jsonContentHeader = "application/json";
    private static final String authorizationHeader = "Authorization";
    private static final String createCoursePath = "/api/courses/create";
    private static final String getCoursePath = "/api/courses/get/courses/";
    private static final String courseCode = "CSE2216";
    private static final String createGradePath = "/api/courses/create/grade";
    private static final ZonedDateTime date = ZonedDateTime.now();
    private static final CourseRequest courseRequest = new CourseRequest(courseCode,
            date, date, 1);

    @Autowired
    private transient CourseRepository courseRepository;

    @Autowired
    private transient GradeRepository gradeRepository;

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
        courseRepository.deleteAll();
        gradeRepository.deleteAll();

        //mock the jwtUtils
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claimsJwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn("ADMIN");
        when(jwtUtils.getUserId(Mockito.any())).thenReturn((long) 1);

    }

    @Test
    void testCourseCreationWithNoConflictingCourses() throws Exception {

        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testCourseCreationWithConflictingCourses() throws Exception {

        mockMvc.perform(post(createCoursePath)
                        .header(authorizationHeader, "")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isOk());

        mockMvc.perform(post(createCoursePath)
                        .header(authorizationHeader, "")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void testCourseCreationWithNoAuthorization() throws Exception {


        mockMvc.perform(post(createCoursePath)
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isForbidden());

    }

    @Test
    void testGettingCoursesByCode() throws Exception {
        //First we add the course

        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //now we check if we can get the list of courses by course code.
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content,
                new TypeReference<List<CourseResponse>>(){});
        //Expected Course Response
        Assert.assertNotNull(courses);
        Assert.assertNotNull(courses.get(0));
        Assert.assertEquals(1, courses.size());
        Assert.assertEquals(courseCode, ((CourseResponse) courses.get(0)).getCourseCode());
    }

    //testing for null or empty courses.
    @Test
    void testEmptyCoursesListReturned() throws Exception {

        mockMvc.perform(get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthroizedGettingOfCoursesList() throws Exception {
        mockMvc.perform(get(getCoursePath + courseCode))
                .andExpect(status().isForbidden());
    }

    @Test
    void gettingCourseByItsId() throws Exception {
        //First we add the course


        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());
        //now we check if it has the correct course id.
        MvcResult mvcResult = mockMvc.perform(get("/api/courses/get/1")
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        CourseResponse response = objectMapper.readValue(content, CourseResponse.class);

        Assert.assertNotNull(response);
        Assert.assertEquals(courseRequest.getCourseCode(), response.getCourseCode());
    }

    @Test
    void gettingAcourseWhichDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/courses/get/1")
                .header(authorizationHeader, ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthroziedAccessForGettingCourse() throws Exception {
        mockMvc.perform(get("/api/courses/get/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletingAnExistingCourse() throws Exception {
        //First we add the course

        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content,
                new TypeReference<List<CourseResponse>>(){});
        //now we delete the course
        mockMvc.perform(post("/api/courses/delete/" + courses.get(0).getCourseId())
                .header(authorizationHeader, ""))
                .andExpect(status().isOk());

    }

    @Test
    void deletingAnonExistentCourse() throws Exception {
        mockMvc.perform(post("/api/courses/delete/1")
                .header(authorizationHeader, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noAuthorizationForDeletingCourses() throws Exception {
        mockMvc.perform(post("/api/courses/delete/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addingAgradeObject() throws Exception {
        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(
                get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content,
                new TypeReference<List<CourseResponse>>(){});
        long courseid = courses.get(0).getCourseId();
        //now we begin the grade id's test

        GradeRequest gradeRequest = new GradeRequest(courseid, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void addingGradeWhenGradeAlreadyExists() throws Exception {
        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content,
                new TypeReference<List<CourseResponse>>(){});
        long courseid = courses.get(0).getCourseId();
        //now we begin the grade id's test

        GradeRequest gradeRequest = new GradeRequest(courseid, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post(createGradePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notAuthorizedAddingOfGrades() throws Exception {
        GradeRequest gradeRequest = new GradeRequest(1, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGradeOfAuser() throws Exception {

        mockMvc.perform(post(createCoursePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

        //get the course
        MvcResult mvcResult = mockMvc.perform(get(getCoursePath + courseCode)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        List<CourseResponse> courses = objectMapper.readValue(content,
                new TypeReference<List<CourseResponse>>(){});
        long courseid = courses.get(0).getCourseId();
        //now we begin the grade id's test
        GradeRequest gradeRequest = new GradeRequest(courseid, 5.75f, 1);

        mockMvc.perform(post(createGradePath)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk());

        //adds the grade. Now we get the grade.
        MvcResult mvcResult1 = mockMvc.perform(get("/api/courses/get/grade/1/" + courseid)
                .header(authorizationHeader, ""))
                .andExpect(status().isOk())
                .andReturn();
        String content2 = mvcResult1.getResponse().getContentAsString();
        float grade = Float.parseFloat(content2);

        Assert.assertEquals(5.75f, grade, 0.1f);

    }

    @Test
    void testNoGradeFound() throws Exception {

        mockMvc.perform(get("/api/courses/get/grade/1/1")
                .header(authorizationHeader, ""))
                .andExpect(status().isNotFound());

    }

    @Test
    void testNoAuthroizationToRetrieveGrades() throws Exception {
        mockMvc.perform(get("/api/courses/get/grade/1/1"))
                .andExpect(status().isForbidden());
    }


    //testing the isAuthroizedMethod
    @Test
    void testIsNoToken() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add(authorizationHeader, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn(null);

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assert.assertNull(result);
    }

    //testing the isAuthroizedMethod
    @Test
    void testIsNoJwsClaims() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add(authorizationHeader, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("Valid");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(null);

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assert.assertNull(result);
    }

    //testing the isAuthroizedMethod
    @Test
    void testIsNotCorrectRole() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add(authorizationHeader, "Bearer test");
        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("Valid");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(claimsJwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn("STUDENT");

        Jws<Claims> result = courseController.isAuthorized(headers);
        Assert.assertEquals("STUDENT", jwtUtils.getRole(result));
        Assert.assertEquals(true, courseController.checkIfStudent(result));
        Assert.assertNotEquals("LECTURER", jwtUtils.getRole(result));
        Assert.assertEquals(false, courseController.checkIfLecturer(result));

    }
}
