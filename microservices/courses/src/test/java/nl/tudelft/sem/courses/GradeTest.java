package nl.tudelft.sem.courses;

import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.entities.Role;
import nl.tudelft.sem.courses.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GradeTest {


    @Test
    public void constructorTest(){
        Course course = new Course("CSE2215");
        User user = new User(1,  Role.STUDENT);
        long id = 1;
        Grade grade = new Grade(id, course, user, 2.0f);

        assertNotNull(grade);
    }

    @Test
    public void testingEqualsMethod(){
        Course course = new Course("CSE2215");
        User user = new User(1, Role.STUDENT);
        long id = 1;
        long id2 = 2;
        Grade grade = new Grade(id, course, user, 2.0f);
        Grade grade2 = new Grade(id, course, user, 3.0f);

        assertNotEquals(grade, grade2);
    }
}
