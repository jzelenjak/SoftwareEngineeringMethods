package nl.tudelft.sem.courses.unittests;


import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import org.junit.jupiter.api.Test;




public class GradeTest {


    @Test
    public void constructorTest() {
        Course course = new Course(1, "CSE2215", LocalDateTime.now());
        long id = 1;
        long userId = 5;
        Grade grade = new Grade(id, course, userId, 2.0f);

        assertNotNull(grade);
    }

    @Test
    public void testingEqualsMethod() {
        Course course = new Course(1, "CSE2215", LocalDateTime.now());

        long id = 1;
        long id2 = 2;

        long userId = 5;
        long userId2 = 3;

        Grade grade = new Grade(id, course, userId, 2.0f);
        Grade grade2 = new Grade(id2, course, userId2, 3.0f);

        assertNotEquals(grade, grade2);
    }
}
