package nl.tudelft.sem.courses.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nl.tudelft.sem.courses.entities.Course;
import org.junit.jupiter.api.Test;


public class CourseTest {

    @Test
    public void testingConstructor() {
        String courseCode = "CSE2215";
        Course course = new Course(1, courseCode, LocalDateTime.now(), LocalDateTime.now());
        assertNotNull(course);
    }

    @Test
    public void testingEquals() {
        String courseCode = "CSE2215";
        String course2Code = "CSE2225";
        LocalDateTime time = LocalDateTime.now();

        Course course = new Course(1, courseCode, time, time);
        Course course2 = new Course(2, course2Code, time, time);
        Course course3 = new Course(1, courseCode, time, time);
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testingToString() {
        String courseCode = "CSE2215";
        Course course = new Course(1, courseCode, LocalDateTime.now(), LocalDateTime.now());
        String testString = "Course{"
                + "course code ='" + courseCode + '\''
                + ", users=" + "[]"
                + '}';
        assertEquals(testString, course.toString());
    }
}
