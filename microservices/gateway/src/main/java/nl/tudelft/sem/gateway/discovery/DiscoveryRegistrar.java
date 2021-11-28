package nl.tudelft.sem.gateway.discovery;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/discovery")
public class DiscoveryRegistrar {

    // Keeps track of all registered microservices
    Map<String, DiscoveryRegistry> registries;

    /**
     * Construct the registrar object.
     */
    public DiscoveryRegistrar() {
        this.registries = new HashMap<>();
    }

    @GetMapping("/{target}")
    @ResponseStatus(HttpStatus.OK)
    private @ResponseBody String getMicroserviceInfo(@PathVariable("target") String target) {
        if (!registries.containsKey(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Targeted microservice not found or does not exist");
        }

        return registries.get(target).getRegistration();
    }

    @PostMapping("/register/{target}")
    @ResponseStatus(HttpStatus.OK)
    private void registerMicroservice(@PathVariable("target") String target, @RequestBody @Valid Registration registration) {
        if (!registries.containsKey(target)) {
            registries.put(target, new DiscoveryRegistry());
        }

        registries.get(target).addRegistration(registration);
    }
}
