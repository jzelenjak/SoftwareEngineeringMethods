package nl.tudelft.sem.hiring.procedure.recommendation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecommendationTest {
    private transient Recommendation recommendation;

    @BeforeEach
    void setup() {
        recommendation = new Recommendation(5205205L, 9.9);
    }

    @Test
    void toJsonTest() {
        String jsonActual = recommendation.toJson();
        String jsonExpected = String.format("{\r\n  \"userId\" : %d,\r\n  \"metric\" : %.1f\r\n}",
                recommendation.getUserId(), recommendation.getMetric());
        System.out.println(jsonExpected);
        System.out.println(jsonActual);
        Assertions.assertEquals(jsonExpected, jsonActual);
    }
}
