package nl.tudelft.sem.hiring.procedure.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
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
 * The class that implements RecommendationStrategy interface by recommending candidate TAs
 *      based of the number of times they have been selected as a TA for
 *      any edition of the given course.
 */
public class TimesSelectedStrategy implements RecommendationStrategy {
    private final transient GatewayConfig gatewayConfig;

    private final transient WebClient webClient;

    private final transient ApplicationRepository repo;

    /**
     * Instantiates a new TimesSelectedStrategy object.
     *
     * @param repo the TA application repository
     */
    public TimesSelectedStrategy(ApplicationRepository repo, GatewayConfig gatewayConfig) {
        this.repo = repo;
        this.webClient = WebClient.create();
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Recommends at most the specified number of candidate TAs who have applied
     *   for a specified course.
     *   It uses the strategy of the max times selected for any edition of the given course.
     *
     * @param courseId      the id of the course
     * @param number        the maximum number of recommendations to return
     * @param minValue      the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'number'.
     */
    @Override
    public Mono<List<Recommendation>> recommend(long courseId, int number, double minValue) {
        return
           this.webClient
            .get()
            .uri(buildUri(gatewayConfig.getHost(), gatewayConfig.getPort(), courseId,
            "api", "courses", "get-all-editions"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .flatMap(response -> processMono(response, courseId, number, minValue));
    }

    /**
     * A helper method to process the received mono response.
     *
     * @param response   the received mono response
     * @param courseId   the id of the course
     * @param number     the maximum number of recommendations to return
     * @param minValue   the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'number'.
     */
    private Mono<List<Recommendation>> processMono(ClientResponse response, long courseId,
                                                        int number, double minValue) {
        if (response.statusCode().isError()) {
            return Mono
                    .error(new ResponseStatusException(response.statusCode(),
                            "Could not make any recommendations"));
        }
        return response
                .bodyToMono(String.class)
                .flatMap(body -> processMonoBody(body, courseId, number, minValue));
    }

    /**
     * A helper method to process the received mono response body.
     *
     * @param body      the body from the received mono response
     * @param courseId  the id of the course
     * @param number    the maximum number of recommendations to return
     * @param minValue  the minimum value for the metric (used for filtering)
     * @return the list of recommendations for candidate TAs based on the number of
     *         times selected for a TA position for one course (wrapped in the mono).
     *         The size of the list is at most 'number'.
     */
    private Mono<List<Recommendation>> processMonoBody(String body, long courseId,
                                                   int number, double minValue) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonList = mapper.readTree(body).get("courseIds").toString();
            List<Long> courseIds = mapper.readValue(jsonList, new TypeReference<List<Long>>() {});
            return Mono
                    .just(this.repo
                            .findTopByTimesSelected(courseId, courseIds, (long) minValue)
                            .stream()
                            .limit(number)
                            .map(a -> new Recommendation((Long) a[0], ((Long) a[1]).doubleValue()))
                            .collect(Collectors.toList())
                    );
        } catch (JsonProcessingException e) {
            return Mono
                    .error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error has occurred. Please try again later!"));
        }
    }


    /**
     * A helper method to create a URI for HTTP request.
     *
     * @param host      the host in the url
     * @param port      the port in the url
     * @param courseId  the id of the course
     * @param path      the path in the url
     * @return          the complete String url
     */
    private String buildUri(String host, int port, long courseId, String... path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .port(port)
                .pathSegment(path)
                .queryParam("courseid", courseId)
                .toUriString();
    }
}
