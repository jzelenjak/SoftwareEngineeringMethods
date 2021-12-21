package nl.tudelft.sem.courses.unittests;

import nl.tudelft.sem.courses.entities.Teaches;
import nl.tudelft.sem.courses.entities.TeachesPk;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

public class TeachesTest {

    private static final long courseId = 1;
    private static final long lecturerId = 2;

    @Test
    public void testCreatingLecturer() {
        Teaches teaches = new Teaches(courseId, lecturerId);
        Assert.assertTrue(teaches.hashCode() >= 0);
        Assert.assertEquals(teaches.getLecturerId(), lecturerId);
        Assert.assertEquals(teaches.getCourseId(), courseId);
    }

    @Test
    public void testNoArgsConstructor() {
        Teaches teaches = new Teaches();
        teaches.setLecturerId(lecturerId);
        teaches.setCourseId(courseId);

        Assert.assertEquals(teaches.getLecturerId(), lecturerId);
        Assert.assertEquals(teaches.getCourseId(), courseId);
    }

    @Test
    public void testEqualsMethod() {
        Teaches teaches1 = new Teaches(1, 1);
        Teaches teaches2 = new Teaches(1, 1);
        Boolean isEqual = teaches1.equals(teaches2);
        Assert.assertTrue(isEqual);
    }


    @Test
    public void testIsNotEqualCourseIdMethod() {
        Teaches teaches1 = new Teaches(1, 1);
        Teaches teaches2 = new Teaches(2, 1);
        Boolean isEqual = teaches1.equals(teaches2);
        Assert.assertFalse(isEqual);
    }


    @Test
    public void testIsNotEqualLecturerIdMethod() {
        Teaches teaches1 = new Teaches(1, 1);
        Teaches teaches2 = new Teaches(1, 2);
        Boolean isEqual = teaches1.equals(teaches2);
        Assert.assertFalse(isEqual);
    }


    @Test
    public void equalsNull() {
        Teaches teaches  = new Teaches(10, 3);
        Teaches teaches2 = null;
        boolean isEqual = teaches.equals(teaches2);
        Assert.assertFalse(isEqual);
    }

    @Test
    public void equalsSameObject() {
        Teaches teaches  = new Teaches(10, 3);
        boolean isEqual = teaches.equals(courseId);
        Assert.assertFalse(isEqual);
    }


    @Test
    public void equalsnotSameClass() {
        Teaches teaches  = new Teaches(10, 3);
        boolean isEqual = teaches.equals(teaches);
        Assert.assertTrue(isEqual);
    }

    @Test
    public void teachesPkTest() {
        TeachesPk pk = new TeachesPk(1L, 1L);
        Assert.assertNotNull(pk);
    }
}
