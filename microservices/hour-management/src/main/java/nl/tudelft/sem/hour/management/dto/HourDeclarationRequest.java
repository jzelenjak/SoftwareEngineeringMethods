package nl.tudelft.sem.hour.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HourDeclarationRequest {
    private long studentId;

    private long courseId;

    private double declaredHours;
}
