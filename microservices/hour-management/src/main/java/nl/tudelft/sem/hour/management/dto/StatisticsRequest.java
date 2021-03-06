package nl.tudelft.sem.hour.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsRequest {
    private long studentId;

    private long courseId;
}
