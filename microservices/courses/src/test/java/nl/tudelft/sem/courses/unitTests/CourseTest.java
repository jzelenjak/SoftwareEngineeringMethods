package nl.tudelft.sem.courses.unitTests;

import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.User;
import org.junit.jupiter.api.Test;


import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;



public class CourseTest {

    @Test
    public void testingConstructor(){
        String courseID = "CSE2215";
        Course course = new Course(1, courseID, LocalDateTime.now());
        assertNotNull(course);
    }

    @Test
    public void testingEquals(){
        String courseID = "CSE2215";
        String course2ID = "CSE2225";
        Course course = new Course(1, courseID, LocalDateTime.now());
        Course course2 = new Course(2, course2ID, LocalDateTime.now());
        Course course3 = new Course(1, courseID, LocalDateTime.now());
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testingToString(){
        String courseID = "CSE2215";
        Course course = new Course(1, courseID, LocalDateTime.now());
        String testString = "Course{" +
                "courseID='" + courseID + '\'' +
                ", users=" + "[]" +
                '}';
        assertEquals(testString, course.toString());
    }
}
