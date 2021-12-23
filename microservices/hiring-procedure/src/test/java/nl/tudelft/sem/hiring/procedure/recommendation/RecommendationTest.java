package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecommendationTest {

    @Test
    void testToJson() throws JsonProcessingException {
        Recommendation testObj = new Recommendation(5205205L, 9.9);
        String jsonString = testObj.toJson();

        Assertions
            .assertThat(new ObjectMapper().readValue(jsonString, Recommendation.class))
            .isEqualTo(testObj);
    }
}
