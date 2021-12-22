package nl.tudelft.sem.hiring.procedure.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    @Test
    public void constructorTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(5211111, 1250, time);
        assertEquals(application.getUserId(), 5211111);
        assertEquals(application.getCourseId(), 1250);
        assertEquals(application.getMaxHours(), 200);
    }

    @Test
    public void constructorIdTest() {
        LocalDateTime time = LocalDateTime.now();
        Application application = new Application(1, 5211111, 1250, time);
        assertEquals(application.getUserId(), 5211111);
        assertEquals(application.getCourseId(), 1250);
        assertEquals(application.getApplicationId(), 1);
    }
}
