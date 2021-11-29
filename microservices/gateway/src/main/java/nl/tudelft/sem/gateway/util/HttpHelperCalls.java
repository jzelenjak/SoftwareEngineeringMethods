package nl.tudelft.sem.gateway.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class HttpHelperCalls {

    /**
     * Forwarding helper for GET requests.
     *
     * @param webClient is used for forwarding the request.
     * @param path      is the path to which the request is being forwarded.
     * @return response from remote service.
     */
    public static Mono<String> getMethod(WebClient webClient, String path) {
        return webClient
                .get()
                .uri(path)
                .retrieve()
                .onStatus(httpStatus -> !HttpStatus.OK.equals(httpStatus), response ->
                        response.bodyToMono(String.class)
                                .map(body ->
                                        // TODO
                                        new ResponseStatusException(response.statusCode(), body)))
                .bodyToMono(String.class);
    }

    /**
     * Forwarding helper for DELETE requests.
     *
     * @param webClient is used for forwarding the request.
     * @param path      is the path to which the request is being forwarded.
     * @return response from remote service.
     */
    public static Mono<String> deleteMethod(WebClient webClient, String path) {
        return webClient
                .delete()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Forwarding helper for POST requests.
     *
     * @param webClient is used for forwarding the request.
     * @param path      is the path to which the request is being forwarded.
     * @param body      is the body of the forwarded request.
     * @return response from remote service.
     */
    public static Mono<String> postMethod(WebClient webClient, String path, String body) {
        return webClient
                .post()
                .uri(path)
                .body(Mono.just(body), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Forwarding helper for PUT requests.
     *
     * @param webClient is used for forwarding the request.
     * @param path      is the path to which the request is being forwarded.
     * @param body      is the body of the forwarded request.
     * @return response from remote service.
     */
    public static Mono<String> putMethod(WebClient webClient, String path, String body) {
        return webClient
                .put()
                .uri(path)
                .body(Mono.just(body), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }


}
