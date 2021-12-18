package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * The interface that represents a strategy for recommending a user.
 * It is based on a certain metric (total times selected, times selected for a given course,
 * the highest grade or the most hours spent working on the given course)
 */
public interface RecommendationStrategy {

    /**
     * Recommends at most the specified number of users who have applied for the specified course.
     *
     * @param courseId the id of the course
     * @param amount   the maximum number of recommendations for the course
     * @param minValue the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs.
     *         The size of the list is at most `amount`
     */
    Mono<List<Recommendation>> recommend(long courseId, int amount, double minValue);


    /**
     * A helper method that converts a JSON map Long -> Double (in its JSON representation)
     *      to a list of Recommendations.
     * Used in HoursStrategy and GradeStrategy when the userIds and their metrics are
     *      received from Courses and Hour Management microservices.
     *
     * @param json  JSON representation of map: Long -> Double
     * @return the list of recommendations for candidate TAs
     * @throws JsonProcessingException when an error occurs while processing JSON string
     */
    default List<Recommendation> convertJsonToRecommendationList(String json)
                throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<Long, Double> grades = mapper.readValue(json, new TypeReference<Map<Long, Double>>(){});

        return grades
                .entrySet()
                .stream()
                .map((entry) -> new Recommendation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * A helper method that converts a JSON list of Longs (in its JSON representation)
     *   to a Java List of Longs.
     * Used in HoursStrategy and TimesSelectedStrategy when the courseIds with the same
     *   course code are received from Courses microservice.
     *
     * @param json  JSON representation of a list of longs ( "fieldName" : [...] )
     * @param field the field whose value is the list of longs
     * @return the Java List of Longs
     * @throws JsonProcessingException when an error occurs while processing JSON string
     */
    default List<Long> convertJsonToLongList(String json, String field)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonList = mapper.readTree(json).get(field).toString();
        return mapper.readValue(jsonList, new TypeReference<List<Long>>() {});
    }

    /**
     * A helper method to create a URI with no query parameters for an HTTP request.
     *
     * @param host the host in the URL
     * @param port the port in the URL
     * @param path the path in the URL
     * @return the complete String URL
     */
    default String buildUri(String host, int port, String... path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .pathSegment(path)
                .toUriString();
    }

    /**
     * A helper method to create a URI with a query parameter "courseId" for an HTTP request.
     *
     * @param host     the host in the URL
     * @param port     the port in the URL
     * @param courseId the id of the course
     * @param path     the path in the URL
     * @return the complete String URL
     */
    default String buildUriWithCourseId(String host, int port,
                                        long courseId, String... path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .pathSegment(path)
                .queryParam("courseId", courseId)
                .toUriString();
    }
}
