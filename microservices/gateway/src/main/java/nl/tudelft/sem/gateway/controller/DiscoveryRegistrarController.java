package nl.tudelft.sem.gateway.controller;

import javax.validation.Valid;
import nl.tudelft.sem.gateway.discovery.Registration;
import nl.tudelft.sem.gateway.service.DiscoveryRegistrarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DiscoveryRegistrarController {

    // Logger
    private static final Logger logger = LoggerFactory
            .getLogger(DiscoveryRegistrarController.class);

    // Registrar service class
    private final transient DiscoveryRegistrarService discoveryRegistrarService;

    /**
     * Constructs a DiscoveryRegistrarController instance.
     *
     * @param discoveryRegistrarService is an automatically injected dependency.
     */
    @Autowired
    public DiscoveryRegistrarController(DiscoveryRegistrarService discoveryRegistrarService) {
        this.discoveryRegistrarService = discoveryRegistrarService;
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
        Registration registration = discoveryRegistrarService.getRegistrationIfExists(target);
        if (registration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find active microservice registration for '"
                            + target
                            + "' to forward request to");
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

        // Add the registration
        discoveryRegistrarService.addRegistration(target, registration);
    }
}
