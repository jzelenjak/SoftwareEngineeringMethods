package nl.tudelft.sem.courses.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nl.tudelft.sem.courses.entities.Course;
import org.junit.jupiter.api.Test;


public class CourseTest {
    private static final String courseCode = "CSE2215";


    @Test
    public void testingConstructorBig() {
        Course course = new Course(1, courseCode, LocalDate.now(), LocalDate.now());
        assertNotNull(course);
        //testing the getters and setters

        assertEquals(1, course.getId());
        assertEquals(courseCode, course.getCourseCode());
        assertEquals(LocalDate.now(), course.getStartDate());
        assertEquals(LocalDate.now(), course.getFinishDate());


    }


    @Test
    public void testingConstructorSmall() {
        Course course = new Course(courseCode, LocalDate.now(), LocalDate.now());
        assertNotNull(course);
        //testing the getters and setters
        int hashcode = course.hashCode();
        assertTrue(hashcode >= 0);
        assertEquals(courseCode, course.getCourseCode());
        assertEquals(LocalDate.now(), course.getStartDate());
        assertEquals(LocalDate.now(), course.getFinishDate());


    }

    @Test
    public void testingEquals() {

        String course2Code = "CSE2225";
        LocalDate time = LocalDate.now();

        Course course = new Course(1, courseCode, time, time);
        Course course2 = new Course(2, course2Code, time, time);
        Course course3 = new Course(1, courseCode, time, time);
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testingToString() {
        Course course = new Course(1, courseCode, LocalDate.now(), LocalDate.now());
        String testString = "Course{"
                + "course code='" + courseCode + '\''
                + ", users=" + "[]"
                + '}';
        assertEquals(testString, course.toString());
    }


    @Test
    public void testingEqualsNotSameClass() {
        Course course = new Course(1, courseCode, LocalDate.now(), LocalDate.now());

        boolean result = course.equals(LocalDate.now());
        assertFalse(result);

    }

    @Test
    public void startDatesAreDifferent() {
        String course2Code = "CSE2225";
        LocalDate time = LocalDate.now();

        Course course = new Course(1, courseCode, time, time);
        Course course2 = new Course(2, course2Code, LocalDate.MAX, time);

        boolean result = course.equals(course2);
        assertFalse(result);
    }

}
