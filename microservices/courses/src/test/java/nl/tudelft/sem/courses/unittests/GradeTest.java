package nl.tudelft.sem.courses.unittests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import org.junit.jupiter.api.Test;




public class GradeTest {


    @Test
    public void constructorTest() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        assertNotNull(grade);
    }

    @Test
    public void testingEqualsMethod() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());

        long id = 1;
        long id2 = 2;

        long userId = 5;
        long userId2 = 3;

        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(id2, course, userId2, 3.0f);

        assertNotEquals(grade, grade2);
    }

    @Test
    public void testingToString() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());
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
    public void notTheSameClass() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        boolean result = grade.equals(LocalDate.now());
        assertFalse(result);
    }

    @Test
    public void gradeValueIsDifferent() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(2,course, userId, 3.0f);
        boolean result = grade.equals(grade2);
        assertFalse(result);
    }

    @Test
    public void coursesAreDifferent() {
        Course course = new Course(1, "CSE2215", LocalDate.now(), LocalDate.now());
        Course course2 = new Course(2, "CSE2216", LocalDate.MAX, LocalDate.now());

        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(2,course2, userId, 2.0f);
        boolean result = grade.equals(grade2);
        assertFalse(result);
    }

}
