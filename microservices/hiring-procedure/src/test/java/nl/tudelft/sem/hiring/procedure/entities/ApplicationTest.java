package nl.tudelft.sem.hiring.procedure.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    @Test
    public void constructorTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(5211111, 1250, time);
        assertEquals(application.getUserId(), 5211111);
        assertEquals(application.getCourseId(), 1250);
    }

    @Test
    public void constructorIdTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(1, 5211111, 1250, time);
        assertEquals(application.getUserId(), 5211111);
        assertEquals(application.getCourseId(), 1250);
        assertEquals(application.getApplicationId(), 1);
    }

    @Test
    public void equalsTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application1 = new Application(1, 5211111, 1250, time);
        Application application2 = new Application(1, 5211111, 1250, time);
        assertEquals(application1, application2);
    }

    @Test
    public void equalsSameTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(1, 5211111, 1250, time);
        assertEquals(application, application);
    }

    @Test
    public void equalsNullTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(1, 5211111, 1250, time);
        assertNotEquals(application, null);
    }

    @Test
    public void toStringTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(1, 5211111, 1250, time);
        String s = "Application{"
            + "applicationId=" + 1
            + ", userId=" + 5211111
            + ", courseId=" + 1250
            + ", status=" + 0
            + ", submissionDate=" + application.getSubmissionDate()
            + ", lastUpdate=" + application.getLastUpdate()
            + '}';
        assertEquals(application.toString(), s);
    }
}
