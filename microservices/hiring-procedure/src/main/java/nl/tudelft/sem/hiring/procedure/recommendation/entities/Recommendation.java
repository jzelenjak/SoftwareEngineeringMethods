package nl.tudelft.sem.hiring.procedure.recommendation.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents a recommendation:
 *   it has the userId and the metric that is used for recommendation
 *   (e.g. total times selected, times selected, grade, hours worked etc.)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recommendation {
    private long userId;
    private double metric;

    /**
     * Transforms recommendation into json string.
     *
     * @return string
     */
    public String toJson() {
        return new ObjectMapper().createObjectNode()
                .put("userId", this.userId)
                .put("metric", this.metric)
                .toPrettyString();
    }
}
