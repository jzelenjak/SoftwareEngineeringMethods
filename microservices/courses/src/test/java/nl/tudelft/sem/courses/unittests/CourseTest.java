package nl.tudelft.sem.courses.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import nl.tudelft.sem.courses.entities.Course;
import org.junit.jupiter.api.Test;


public class CourseTest {

    @Test
    public void testingConstructor() {
        String courseId = "CSE2215";
        Course course = new Course(1, courseId, LocalDateTime.now());
        assertNotNull(course);
    }

    @Test
    public void testingEquals() {
        String courseId = "CSE2215";
        String course2Id = "CSE2225";
        Course course = new Course(1, courseId, LocalDateTime.now());
        Course course2 = new Course(2, course2Id, LocalDateTime.now());
        Course course3 = new Course(1, courseId, LocalDateTime.now());
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testingToString() {
        String courseId = "CSE2215";
        Course course = new Course(1, courseId, LocalDateTime.now());
        String testString = "Course{"
                + "courseID='" + courseId + '\''
                + ", users=" + "[]"
                + '}';
        assertEquals(testString, course.toString());
    }
}
