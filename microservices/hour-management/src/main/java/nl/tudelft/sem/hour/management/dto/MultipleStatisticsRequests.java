package nl.tudelft.sem.hour.management.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipleStatisticsRequests {
    private Set<Long> studentIds;

    private Set<Long> courseIds;
}
