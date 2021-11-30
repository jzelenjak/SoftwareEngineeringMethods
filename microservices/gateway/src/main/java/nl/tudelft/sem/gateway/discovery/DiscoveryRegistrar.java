package nl.tudelft.sem.gateway.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/discovery")
public class DiscoveryRegistrar {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryRegistrar.class);

    // Keeps track of all registered microservices
    @Getter
    private final Map<String, DiscoveryRegistry> registries;

    /**
     * Construct the registrar object.
     */
    public DiscoveryRegistrar() {
        this.registries = new HashMap<>();
    }

    /**
     * Request handler for the /discovery/{target} endpoint. Attempts to find a registry for the
     * target.
     *
     * @param target is the targeted microservice for which a registration is requested.
     * @return the registration.
     */
    @GetMapping("/{target}")
    @ResponseStatus(HttpStatus.OK)
    private @ResponseBody
    Registration getMicroserviceInfo(@PathVariable("target") String target) {
        if (!registries.containsKey(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find active microservice registration for '"
                            + target + "' to forward request to");
        }

        Registration registration = registries.get(target).getRegistration();
        if (registration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find active microservice registration for '"
                            + target + "' to forward request to");
        }

        // Return the base URL to the targeted microservice
        return registration;
    }

    /**
     * Registers a new endpoint for the specified target. Endpoint can also be used to perform a
     * heartbeat.
     *
     * @param target       is the targeted microservice base that the microservice is being
     *                     registered to.
     * @param registration is the registration info of the to-be-registered microservice.
     */
    @PostMapping("/register/{target}")
    @ResponseStatus(HttpStatus.OK)
    private void registerMicroservice(@PathVariable("target") String target,
                                      @RequestBody @Valid Registration registration) {
        // Log action
        logger.debug("Registering/updating entry for target: '{}' pointing to: '{}:{}'", target,
                registration.getHost(), registration.getPort());

        // Process registration
        if (!registries.containsKey(target)) {
            registries.put(target, new DiscoveryRegistry());
        }

        // Attempt to add the registration to the registry
        try {
            registries.get(target).addRegistration(registration);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to add registration to the registration pool due to internal errors");
        }
    }
}
