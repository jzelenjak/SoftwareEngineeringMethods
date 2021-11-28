package nl.tudelft.sem.gateway.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class HttpHelperCalls {
    public static Mono<String> getMethod(WebClient webClient, String path) {
        return webClient
                .get()
                .uri(path)
                .retrieve()
                .onStatus(httpStatus -> !HttpStatus.OK.equals(httpStatus),
                          response -> response.bodyToMono(String.class)
                                  .map(body -> new ResponseStatusException(response.statusCode(), body))) // TODO
                .bodyToMono(String.class);
    }

    public static Mono<String> deleteMethod(WebClient webClient, String path) {
        return webClient
                .delete()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<String> postMethod(WebClient webClient, String path, String body) {
        return webClient
                .post()
                .uri(path)
                .body(Mono.just(body), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<String> putMethod(WebClient webClient, String path, String body) {
        return webClient
                .put()
                .uri(path)
                .body(Mono.just(body), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }


}
