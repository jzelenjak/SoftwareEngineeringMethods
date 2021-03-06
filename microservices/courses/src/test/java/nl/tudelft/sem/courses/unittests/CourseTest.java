package nl.tudelft.sem.courses.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import nl.tudelft.sem.courses.entities.Course;
import org.junit.jupiter.api.Test;


public class CourseTest {
    private static final String courseCode = "CSE2215";
    private static final ZonedDateTime date = ZonedDateTime.now();
    private static final Course course = new Course(1, courseCode, date, date, 1);


    @Test
    public void testConstructorBig() {
        assertNotNull(course);
        //testing the getters and setters

        assertEquals(1, course.getId());
        assertEquals(courseCode, course.getCourseCode());
        assertEquals(date, course.getStartDate());
        assertEquals(date, course.getFinishDate());


    }


    @Test
    public void testConstructorSmall() {
        assertNotNull(course);
        //testing the getters and setters
        int hashcode = course.hashCode();
        assertTrue(hashcode >= 0);
        assertEquals(courseCode, course.getCourseCode());
        assertEquals(date, course.getStartDate());
        assertEquals(date, course.getFinishDate());


    }

    @Test
    public void testEquals() {

        String course2Code = "CSE2225";
        ZonedDateTime time = ZonedDateTime.now();

        Course course = new Course(1, courseCode, time, time, 1);
        Course course2 = new Course(2, course2Code, time, time, 1);
        Course course3 = new Course(1, courseCode, time, time, 1);
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testToString() {
        String testString = "Course{"
                + "course code='" + courseCode + '\''
                + ", users=" + "[]"
                + '}';
        assertEquals(testString, course.toString());
    }


    @Test
    public void testEqualsNotSameClass() {
        boolean result = course.equals(LocalDate.now());
        assertFalse(result);

    }

    @Test
    public void testEqualstartDatesAreDifferent() {
        String course2Code = "CSE2225";
        ZonedDateTime time = ZonedDateTime.now();

        Course course = new Course(1, courseCode, time, time, 1);
        Course course2 = new Course(2, course2Code,
                ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), time, 1);

        boolean result = course.equals(course2);
        assertFalse(result);
    }

    @Test
    public void testEqualsOneObjectIsNull() {
        Course course = new Course(1, courseCode, date, date, 1);
        Course course2 = null;
        boolean result = course.equals(course2);
        assertFalse(result);
    }

    @Test
    public void testEqualsStartDatesSameEndDatesDifferent() {
        Course course = new Course(1, courseCode, date, date, 1);
        Course course2 = new Course(2, courseCode, date,
                ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), 1);
        boolean result = course.equals(course2);
        assertFalse(result);
    }

    @Test
    public void testEqualsCoursesAreEqual() {
        Course course = new Course(1, courseCode, date, date, 1);
        Course course2 = new Course(2, courseCode, date, date, 1);
        boolean result = course.equals(course2);
        assertTrue(result);
    }

    @Test
    public void testEqualsNumberOfStudentsDifferent() {
        Course course = new Course(1, courseCode, date, date, 1);
        Course course2 = new Course(2, courseCode, date, date, 2);
        boolean result = course.equals(course2);
        assertFalse(result);
    }

    @Test
    public void testEqualsDateEqualsMethod() {
        boolean result = course.datesEqual(date, date);
        assertTrue(result);
    }

    @Test
    public void testEqualsDatesDaysAreNotEqual() {
        ZonedDateTime time = ZonedDateTime.parse("2021-12-03T10:15:30+01:00[Europe/Paris]");
        boolean result = course.datesEqual(date, time);
        assertFalse(result);
    }

    @Test
    public void testDatesMonthsAreNotEqual() {
        ZonedDateTime time = ZonedDateTime.parse("2021-11-03T10:15:30+01:00[Europe/Paris]");
        boolean result = course.datesEqual(date, time);
        assertFalse(result);
    }
}
