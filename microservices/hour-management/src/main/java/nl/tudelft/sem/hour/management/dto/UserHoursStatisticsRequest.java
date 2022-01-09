package nl.tudelft.sem.hour.management.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHoursStatisticsRequest {
    private int amount;

    private double minHours;

    private Set<Long> userIds;

    private Set<Long> courseIds;
}
