package nl.tudelft.sem.courses.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import org.junit.jupiter.api.Test;


public class GradeTest {
    private static final String courseCode = "CSE2215";
    private static final ZonedDateTime date = ZonedDateTime.now();
    private static final Course course = new Course(1, courseCode, date, date, 1);


    @Test
    public void testConstructor() {
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        assertNotNull(grade);
    }

    @Test
    public void testEqualsMethod() {
        long id = 1;
        long id2 = 2;

        long userId = 5;
        long userId2 = 3;

        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(id2, course, userId2, 3.0f);

        assertNotEquals(grade, grade2);
    }

    @Test
    public void testToString() {
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        String result = "Grade{"
                + "id = " + id
                + ", course=" + course
                + ", user=" + userId
                + '}';

        assertEquals(result, grade.toString());

    }


    @Test
    public void testEqualsDifferentClassType() {
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        boolean result = grade.equals(new Object());
        assertFalse(result);
    }

    @Test
    public void testGradeValueIsDifferent() {
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(2, course, userId, 3.0f);
        boolean result = grade.equals(grade2);
        assertFalse(result);
    }

    @Test
    public void testCoursesAreDifferent() {
        Course course2 = new Course(2, "CSE2216",
                ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), date, 1);

        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(2, course2, userId, 2.0f);
        boolean result = grade.equals(grade2);
        assertFalse(result);
    }

    @Test
    public void testEqualsNullTest() {
        Grade grade = new Grade(2, course, 1, 2.0f);
        assertNotEquals(grade, null);
    }

    @Test
    public void testEqualsSameObject() {
        Grade grade = new Grade(2, course, 1, 2.0f);
        assertEquals(grade, grade);
    }

    @Test
    public void testEqualsIdsAreDifferent() {
        Grade grade = new Grade(2, course, 1, 2.0f);
        Grade grade2 = new Grade(5, course, 2, 2.0f);
        assertNotEquals(grade, grade2);
    }

}
