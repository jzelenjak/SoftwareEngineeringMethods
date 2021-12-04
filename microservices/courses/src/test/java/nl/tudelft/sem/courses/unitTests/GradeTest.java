package nl.tudelft.sem.courses.unitTests;

import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.entities.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GradeTest {


    @Test
    public void constructorTest(){
        Course course = new Course(1, "CSE2215", LocalDateTime.now());
        long id = 1;
        long userID = 5;
        Grade grade = new Grade(id, course, userID, 2.0f);

        assertNotNull(grade);
    }

    @Test
    public void testingEqualsMethod(){
        Course course = new Course(1, "CSE2215", LocalDateTime.now());

        long id = 1;
        long id2 = 2;

        long userID =5;
        long userID2 = 3;

        Grade grade = new Grade(id, course, userID, 2.0f);
        Grade grade2 = new Grade(id2, course, userID2, 3.0f);

        assertNotEquals(grade, grade2);
    }
}
