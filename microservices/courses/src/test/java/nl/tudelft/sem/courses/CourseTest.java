package nl.tudelft.sem.courses;

import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.User;
import org.junit.jupiter.api.Test;


import javax.validation.constraints.Null;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;



public class CourseTest {

    @Test
    public void testingConstructor(){
        String courseID = "CSE2215";
        Course course = new Course(courseID);
        assertNotNull(course);
    }

    @Test
    public void testingEquals(){
        String courseID = "CSE2215";
        String course2ID = "CSE2225";
        Course course = new Course(courseID);
        Course course2 = new Course(course2ID);
        Course course3 = new Course(courseID);
        assertNotEquals(course, course2);

        assertEquals(course, course3);
    }

    @Test
    public void testingToString(){
        String courseID = "CSE2215";
        Course course = new Course(courseID);
        String testString = "Course{" +
                "courseID='" + courseID + '\'' +
                ", user=" + "[]" +
                '}';
        assertEquals(testString, course.toString());
    }
}
