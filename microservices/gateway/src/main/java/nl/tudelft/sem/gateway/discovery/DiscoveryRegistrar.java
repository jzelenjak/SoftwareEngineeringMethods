package nl.tudelft.sem.gateway.discovery;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.Getter;
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
     * @return the base URL of a registered microservice, if exists.
     */
    @GetMapping("/{target}")
    @ResponseStatus(HttpStatus.OK)
    private @ResponseBody
    String getMicroserviceInfo(@PathVariable("target") String target) {
        if (!registries.containsKey(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Targeted microservice not found or does not exist");
        }

        Registration registration = registries.get(target).getRegistration();
        if (registration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find active microservice registration to forward request");
        }

        // Return the base URL to the targeted microservice
        return registration.getPath();
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
        if (!registries.containsKey(target)) {
            registries.put(target, new DiscoveryRegistry());
        }

        registries.get(target).addRegistration(registration);
    }
}
