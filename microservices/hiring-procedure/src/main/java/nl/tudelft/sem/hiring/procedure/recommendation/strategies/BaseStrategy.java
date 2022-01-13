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
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * A class that is the base class for all the recommendation strategy objects.
 * It implements RecommendationStrategy interface and thus requires all of its child classes
 *  implement this interface.
 * In addition, this class stores some member variables and provides some methods that are used
 *  for processing Monos, serialization and deserialization, as well as building URLs
 *  when making requests to other microservices.
 */
public abstract class BaseStrategy implements RecommendationStrategy {

    protected final transient GatewayConfig gatewayConfig;

    protected final transient WebClient webClient;

    protected final transient SubmissionRepository repo;

    protected final transient String authorization;

    private static final transient String SCHEME = "http";

    /**
     * Instantiates a new TimesSelectedStrategy object.
     *
     * @param repo          the TA application repository.
     * @param gatewayConfig the gateway configuration.
     * @param authorization the authorization token of the caller.
     */
    public BaseStrategy(SubmissionRepository repo, GatewayConfig gatewayConfig,
                                 String authorization) {
        this.repo = repo;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
        this.authorization = authorization;
    }

    /**
     * A helper method to send a POST request to another microservice in order to
     *  get some data related to recommendations.
     *
     * @param uri           the URI of the request
     * @param body          the JSON body of the request
     * @param authorization the value of the AUTHORIZATION header of the request
     * @return the client response object wrapped in a Mono.
     */
    protected Mono<ClientResponse> post(String uri, String body, String authorization) {
        return this.webClient
            .post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(body), String.class)
            .exchange();
    }

    /**
     * A helper method to send a GET request to another microservice in order to
     *  get some data related to recommendations. Used for sending a request to courses
     *  microservice to get all the course IDs of the courses with the same course code.
     *
     * @param uri           the URI of the request
     * @param authorization the value of the AUTHORIZATION header of the request
     * @return the client response object wrapped in a Mono.
     */
    protected Mono<ClientResponse> get(String uri, String authorization) {
        return this.webClient
            .get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .exchange();
    }

    /**
     * A helper method to process the received mono response.
     * Used as a callback when flatMapping the received mono.
     *
     * @param response the received mono response
     * @param callback child callback that will be executed on the body of the received mono
     * @return the list of recommendations for candidate TAs based on certain metric
     *          (wrapped in the mono). The size of the list is at most `amount`.
     */
    protected Mono<List<Recommendation>> processMono(ClientResponse response,
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
    protected List<Recommendation> convertJsonToRecommendationList(String json)
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
    protected List<Long> convertJsonToLongList(String json, String field)
        throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonList = mapper.readTree(json).get(field).toString();
        return mapper.readValue(jsonList, new TypeReference<List<Long>>() {
        });
    }

    /**
     * A helper method that converts a list of Recommendations to a Mono.
     *
     * @param list a (possibly empty) list of Recommendations
     * @return the list of recommendations wrapped inside a Mono if the list is not empty.
     *          If the list is empty, a ResponseStatusException with the status 404
     *          is returned, wrapped in a mono.
     */
    protected Mono<List<Recommendation>> recommendationsToMono(List<Recommendation> list) {
        if (list.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Could not make any recommendations"));
        }
        return Mono.just(list);
    }

    /**
     * A helper method to create a URI with no query parameters for an HTTP request.
     *
     * @param host the host in the URL
     * @param port the port in the URL
     * @param path the path in the URL
     * @return the complete String URL
     */
    protected String buildUri(String host, int port, String... path) {
        return UriComponentsBuilder.newInstance()
            .scheme(SCHEME)
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
    protected String buildUriWithCourseId(String host, int port,
                                        long courseId, String... path) {
        return UriComponentsBuilder.newInstance()
            .scheme(SCHEME)
            .host(host)
            .port(port)
            .pathSegment(path)
            .queryParam("courseId", courseId)
            .toUriString();
    }
}
