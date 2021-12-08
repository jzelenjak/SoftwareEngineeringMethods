package nl.tudelft.sem.hour.management.dto;

import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HourDeclarationRequest {
    private long studentId;

    private long courseId;

    private double declaredHours;

    /**
     * Returns the JSON representation of this object.
     *
     * @return JSON representation of the object.
     */
    public String toJson() {
        return String.format(Locale.ROOT,
                "{\"studentId\": %d, \"courseId\": %d, \"declaredHours\": %f}",
                studentId, courseId, declaredHours);
    }
}
