package nl.tudelft.sem.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.gateway.util.HttpHelperCalls;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
     * @param microServiceName is the name of the targeted microservice.
     * @param body             is the body to forward.
     * @param request          is the request info object.
     * @return Response string that becomes available in the future.
     */
    @RequestMapping(value = "/{target}/**", method = {
            RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE
    })
    public @ResponseBody
    Mono<String> getRequest(@PathVariable("target") String microServiceName,
                            @RequestBody(required = false) String body,
                            HttpServletRequest request) {
        // Forward the request
        return webClient.get()
                .uri(ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                        + "/discovery/"
                        + microServiceName)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(s -> {
                    // Determine the destination
                    String destination = s + StringUtils.substringAfter(
                            request.getRequestURL().toString(),
                            "api/" + microServiceName
                    );

                    // Select the appropriate forward method
                    switch (HttpMethod.valueOf(request.getMethod())) {
                        case GET:
                            return HttpHelperCalls.getMethod(webClient, destination);
                        case DELETE:
                            return HttpHelperCalls.deleteMethod(webClient, destination);
                        case POST:
                            return HttpHelperCalls.postMethod(webClient, destination, body);
                        case PUT:
                            return HttpHelperCalls.putMethod(webClient, destination, body);
                        default:
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Unsupported request method type. "
                                            + "Should be [GET, PUT, POST, DELETE]"));
                    }
                });
    }
}
