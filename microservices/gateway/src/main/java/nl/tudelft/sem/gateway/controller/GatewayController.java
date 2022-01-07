package nl.tudelft.sem.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.gateway.discovery.Registration;
import nl.tudelft.sem.gateway.exceptions.MonoForwardingException;
import nl.tudelft.sem.gateway.service.DiscoveryRegistrarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class GatewayController {

    // Create web client for executing asynchronous requests
    private final transient WebClient webClient;

    // Manages the registrations
    private final transient DiscoveryRegistrarService discoveryRegistrarService;

    /**
     * Constructs the GatewayController class.
     */
    @Autowired
    public GatewayController(DiscoveryRegistrarService discoveryRegistrarService) {
        this.webClient = WebClient.create();
        this.discoveryRegistrarService = discoveryRegistrarService;
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
     * @param target  is the name of the targeted microservice.
     * @param body    is the body to forward.
     * @param headers is a collection of headers that is part of the request.
     * @param request is the request info object.
     * @return Response entity containing a string that becomes available in the future.
     */
    @RequestMapping(value = "/{target}/**", method = {
            RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE
    })
    public @ResponseBody
    Mono<ResponseEntity<String>> getRequest(@PathVariable("target") String target,
                                            @RequestBody(required = false) String body,
                                            @RequestHeader HttpHeaders headers,
                                            HttpServletRequest request) {
        // Attempt to retrieve registration
        Registration registration = discoveryRegistrarService.getRegistrationIfExists(target);
        if (registration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find active microservice registration for '"
                            + target
                            + "' to forward request to");
        }

        // Determine the destination
        String destination = ServletUriComponentsBuilder
                .fromRequest(request)
                .host(registration.getHost())
                .port(registration.getPort())
                .toUriString();

        // Forward the request
        return forwardCall(destination, request, body, headers);
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
    private Mono<ResponseEntity<String>> forwardCall(String destination, HttpServletRequest request,
                                                     String body,
                                                     HttpHeaders headers) {
        // Forward call
        return webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(destination)
                .body(Mono.justOrEmpty(body), String.class)
                .headers(header -> header.addAll(headers))
                .exchange()
                .flatMap(response -> response.bodyToMono(String.class)
                        .switchIfEmpty(Mono.just(""))
                        .flatMap(responseBody -> {
                            var responseHeaders = response.headers().asHttpHeaders();

                            if (response.statusCode() != HttpStatus.OK) {
                                return Mono.error(new MonoForwardingException(
                                        response.statusCode(), responseHeaders, responseBody));
                            }
                            return Mono.just(new ResponseEntity<>(responseBody, responseHeaders,
                                    response.statusCode()));
                        })
                );
    }

}
