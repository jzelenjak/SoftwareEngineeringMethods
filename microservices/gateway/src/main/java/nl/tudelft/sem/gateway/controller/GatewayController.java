package nl.tudelft.sem.gateway.controller;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.gateway.util.HttpHelperCalls;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class GatewayController {
    // Create web client for executing asynchronous requests
    @Getter @Setter
    private WebClient webClient;

    /**
     * Constructs the GatewayController class.
     */
    public GatewayController() {
        this.webClient = WebClient.create();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getHelloMessage() {
        return "Hello from Gateway!";
    }

    @RequestMapping(
            value = "/{target}/**",
            method = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE}
    )
    public @ResponseBody Mono<String> getRequest(@PathVariable("target") String microServiceName,
                                                 @RequestBody(required = false) String body,
                                                 HttpServletRequest request) {
        String path = request.getRequestURL().toString();
        HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod());

        String restOfUrl = StringUtils.substringAfter(path, "api/" + microServiceName);

        return webClient.get()
        .uri("http://localhost:8080/discovery/" + microServiceName)
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(s -> {
            String destination = s + restOfUrl;
            switch (requestMethod) {
                case GET:
                    return HttpHelperCalls.getMethod(webClient, destination);
                case DELETE:
                    return HttpHelperCalls.deleteMethod(webClient, destination);
                case POST:
                    return HttpHelperCalls.postMethod(webClient, destination, body);
                case PUT:
                    return HttpHelperCalls.putMethod(webClient, destination, body);
                default: // won't be triggered as Spring does the check for methods
                    return null;
            }
        });
    }
}
