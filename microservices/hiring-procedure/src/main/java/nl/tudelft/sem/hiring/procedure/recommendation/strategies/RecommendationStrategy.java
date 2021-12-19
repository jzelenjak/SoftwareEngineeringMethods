package nl.tudelft.sem.hiring.procedure.recommendation.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ResponseStatusException;
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
     *          The size of the list is at most `amount`
     */
    Mono<List<Recommendation>> recommend(long courseId, int amount, double minValue);


    /**
     * A helper method to process the received mono response.
     * Used as a callback when flatMapping the received mono.
     *
     * @param response the received mono response
     * @param callback child callback that will be executed on the body of the received mono
     * @return the list of recommendations for candidate TAs based on certain metric
     *          (wrapped in the mono). The size of the list is at most `amount`.
     */
    default Mono<List<Recommendation>> processMono(ClientResponse response,
                                 @NotNull Function<String, Mono<List<Recommendation>>> callback) {
        if (response.statusCode().isError()) {
            return Mono.error(new ResponseStatusException(response.statusCode(),
                "Could not make any recommendations"));
        }

        return response
            .bodyToMono(String.class)
            .flatMap(callback)
            .switchIfEmpty(Mono.error(new ResponseStatusException(response.statusCode(),
                "Could not make any recommendations")));
    }


    /**
     * A helper method that converts a JSON map Long -> Double (in its JSON representation)
     * to a list of Recommendations.
     * Used in HoursStrategy and GradeStrategy when the userIds and their metrics are
     * received from Courses and Hour Management microservices.
     *
     * @param json JSON representation of map: Long -> Double
     * @return the list of recommendations for candidate TAs
     * @throws JsonProcessingException when an error occurs while processing JSON string
     */
    default List<Recommendation> convertJsonToRecommendationList(String json)
        throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<Long, Double> grades = mapper.readValue(json, new TypeReference<Map<Long, Double>>() {
        });

        return grades
            .entrySet()
            .stream()
            .map((entry) -> new Recommendation(entry.getKey(), entry.getValue()))
            .sorted((rec1, rec2) -> Double.compare(rec2.getMetric(), rec1.getMetric()))
            .collect(Collectors.toList());
    }

    /**
     * A helper method that converts a JSON list of Longs (in its JSON representation)
     * to a Java List of Longs.
     * Used in HoursStrategy and TimesSelectedStrategy when the courseIds with the same
     * course code are received from Courses microservice.
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
        return mapper.readValue(jsonList, new TypeReference<List<Long>>() {
        });
    }

    /**
     * A helper method that converts a list of Recommendations to a Mono.
     *
     * @param recommendations a (possibly empty) list of Recommendations
     * @return the list of recommendations wrapped inside a Mono if the list is not empty.
     *          If the list is empty, a ResponseStatusException with the status 404
     *          is returned, wrapped in a mono.
     */
    default Mono<List<Recommendation>> recommendationsToMono(List<Recommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Could not make any recommendations"));
        }
        return Mono.just(recommendations);
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
