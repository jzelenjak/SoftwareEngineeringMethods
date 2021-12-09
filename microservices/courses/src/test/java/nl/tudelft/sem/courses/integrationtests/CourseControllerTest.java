package nl.tudelft.sem.courses.integrationtests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;



@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class CourseControllerTest {
    @Autowired
    private transient MockMvc mockMvc;

    private static final String contentType = "Content-Type";
    private static final String jsonContentHeader = "application/json";


    @Autowired
    private CourseRepository courseRepository;


    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private transient ObjectMapper objectMapper;


    @Test
    void testCourseCreationWithNoConflictingCourses() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2215",
                LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/courses/create")
                .contentType(jsonContentHeader)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void testCourseCreationWithConflictingCourses() throws Exception {
        CourseRequest courseRequest = new CourseRequest("CSE2216",
                LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/courses/create")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/create")
                        .contentType(jsonContentHeader)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                        .andExpect(status().isBadRequest());


    }


}
