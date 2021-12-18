package nl.tudelft.sem.hiring.procedure.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class CourseInfoResponseCache {

    // Time it takes for an entry to invalidate
    private static final Duration CACHE_INVALIDATION = Duration.ofMinutes(5);

    // Gateway configuration
    private final transient GatewayConfig gatewayConfig;

    // Cache used to store course info responses
    private final transient Cache<Long, String> courseInfoCache;

    // Webclient used to perform requests to the course information endpoint
    private final transient WebClient webClient;

    /**
     * Constructor of the CourseInfoResponseCache class. Initializes the cache.
     *
     * @param gatewayConfig is the gateway configuration.
     */
    public CourseInfoResponseCache(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        this.courseInfoCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(CACHE_INVALIDATION)
                .build();
        this.webClient = WebClient.create();
    }

    /**
     * Retrieves the course info response from the cache. If the response is not in the cache,
     * fetch it from the course microservice.
     *
     * @param courseId is the id of the course to retrieve the response for.
     * @return the course info response that might become available in the future.
     */
    public Mono<String> getCourseInfoResponse(long courseId) {
        String response = courseInfoCache.getIfPresent(courseId);
        if (response != null) {
            return Mono.just(response);
        }

        // Perform the request to fetch a 'fresh' response
        return webClient.get()
                .uri(UriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(gatewayConfig.getHost())
                        .port(gatewayConfig.getPort())
                        .pathSegment("api", "courses", "get", String.valueOf(courseId))
                        .toUriString())
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Course not found"));
                    }

                    // Add the response to the cache
                    return clientResponse.bodyToMono(String.class).flatMap(body -> {
                        courseInfoCache.put(courseId, body);
                        return Mono.just(body);
                    });
                });
    }

    /**
     * Invalidates the cache.
     */
    public void invalidateCache() {
        courseInfoCache.invalidateAll();
    }

}
