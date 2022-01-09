package nl.tudelft.sem.courses.communication;

import java.util.Set;
import lombok.Data;

@Data
public class MultiCourseRequest {
    // List of course IDs
    Set<Long> courseIds;
}
