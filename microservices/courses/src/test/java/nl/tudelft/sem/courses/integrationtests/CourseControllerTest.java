package nl.tudelft.sem.courses.integrationtests;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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


    @Autowired
    private CourseRepository courseRepository;


    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private transient ObjectMapper objectMapper;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> claimsJwsMock;


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

    //Testing Course Creation Only

    @Test
    void testCourseCreationWithNoConflictingCourses() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2215",
                LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/courses/create/course")
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

    }

//    @Test
    void testCourseCreationWithConflictingCourses() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2216",
                LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/courses/create/course")
                        .header(authorizationHeader, "")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/create/course")
                        .header(authorizationHeader, "")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isBadRequest());


    }

//    @Test
    void testCourseCreationWithNoAuthorization() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2216",
                LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/courses/create/course")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isForbidden());

    }


    //This test doesn't make much sense. Retry again Later.
//    @Test
    void testCourseDeletionAfterCourseCreation() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2216",
                LocalDateTime.now(), LocalDateTime.now());

        MvcResult mvcResult = mockMvc.perform(post("/api/courses/create/course")
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        CourseResponse courseResponse = objectMapper.readValue(content, CourseResponse.class);
        long id = courseResponse.getCourseId();

        mockMvc.perform(post("/api/courses/delete/"+id)
                .header(authorizationHeader, "")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());
    }


}
