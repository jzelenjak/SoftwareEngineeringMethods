package nl.tudelft.sem.courses.communication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    long courseId;
    int amount;
    double minGrade;
    List<Long> userIds;
}
