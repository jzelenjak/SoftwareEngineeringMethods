package nl.tudelft.sem.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.gateway.discovery.Registration;
import nl.tudelft.sem.gateway.exceptions.MonoForwardingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class GatewayController {

    // Create web client for executing asynchronous requests
    @Getter
    @Setter
    private WebClient webClient;

    /**
     * Constructs the GatewayController class.
     */
    public GatewayController() {
        this.webClient = WebClient.create();
    }

    /**
     * Request handler for the base /api/ URL.
     *
     * @return response from gateway.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getHelloMessage() {
        return "Hello from Gateway!";
    }

    /**
     * Request forwarder used to intercept all api calls and forward them to their appropriate
     * destination.
     *
     * @param microserviceName is the name of the targeted microservice.
     * @param body             is the body to forward.
     * @param headers          is a collection of headers that is part of the request.
     * @param request          is the request info object.
     * @return Response string that becomes available in the future.
     */
    @RequestMapping(value = "/{target}/**", method = {
            RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE
    })
    public @ResponseBody
    Mono<String> getRequest(@PathVariable("target") String microserviceName,
                            @RequestBody(required = false) String body,
                            @RequestHeader HttpHeaders headers, HttpServletRequest request) {
        // Forward the request
        return webClient.get()
                .uri(ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .pathSegment("discovery", microserviceName)
                        .toUriString())
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, err ->
                        err.bodyToMono(String.class).map(response ->
                                new MonoForwardingException(err.statusCode(),
                                        err.headers().asHttpHeaders(), response)))
                .bodyToMono(Registration.class)
                .flatMap(redirect -> {
                    // Determine the destination
                    String destination = ServletUriComponentsBuilder
                            .fromRequest(request)
                            .host(redirect.getHost())
                            .port(redirect.getPort())
                            .toUriString();

                    // Forward the call
                    return forwardCall(destination, request, body, headers);
                });
    }

    /**
     * Forwards call to the appropriate microservice.
     *
     * @param destination is the destination address.
     * @param request     is the initial request.
     * @param body        is the body of the initial request.
     * @param headers     is a collection of headers that is part of the initial request.
     * @return response that will become available in the future.
     */
    private Mono<String> forwardCall(String destination, HttpServletRequest request, String body,
                                     HttpHeaders headers) {
        // Forward call
        return webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(destination)
                .body(Mono.justOrEmpty(body), String.class)
                .headers(headers::addAll)
                .retrieve()
                .onStatus(httpStatus -> !HttpStatus.OK.equals(httpStatus), response ->
                        response.bodyToMono(String.class)
                                .map(responseBody ->
                                        new MonoForwardingException(response.statusCode(),
                                                response.headers().asHttpHeaders(), responseBody)))
                .bodyToMono(String.class);
    }

}
