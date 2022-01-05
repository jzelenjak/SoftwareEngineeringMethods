package nl.tudelft.sem.hiring.procedure.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class SubmissionTest {

    @Test
    public void constructorTest() {
        LocalDateTime time = LocalDateTime.now();
        Submission submission = new Submission(5211111, 1250, time);
        assertEquals(submission.getUserId(), 5211111);
        assertEquals(submission.getCourseId(), 1250);
        assertEquals(submission.getMaxHours(), 200);
        assertEquals(submission.getRating(), -1.0);
    }

    @Test
    public void constructorIdTest() {
        LocalDateTime time = LocalDateTime.now();
        Submission submission = new Submission(1, 5211111, 1250, time);
        assertEquals(submission.getUserId(), 5211111);
        assertEquals(submission.getCourseId(), 1250);
        assertEquals(submission.getSubmissionId(), 1);
        assertEquals(submission.getMaxHours(), 200);
        assertEquals(submission.getRating(), -1.0);
    }
}
