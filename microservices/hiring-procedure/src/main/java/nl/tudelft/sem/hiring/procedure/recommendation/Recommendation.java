package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
                .put("metric",this.metric)
                .toPrettyString();
    }
}
