package nl.tudelft.sem.gateway.service;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.Getter;
import nl.tudelft.sem.gateway.discovery.DiscoveryRegistry;
import nl.tudelft.sem.gateway.discovery.Registration;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryRegistrarService {

    // Eviction time for internally stored registrations
    private static final int registryCacheEvictionTimeMinutes = 1;

    // Keeps track of all registered microservices
    @Getter
    private final Map<String, DiscoveryRegistry> registries;

    /**
     * Construct the registrar service object.
     */
    public DiscoveryRegistrarService() {
        this.registries = new HashMap<>();
    }

    /**
     * Checks if there exists a registration for the given target. This does not imply that there is
     * an available microservice for that target.
     *
     * @param target for which the existence check is requested.
     * @return boolean indicator which indicates whether there is a discovery registry for that
     *         object.
     */
    public boolean containsRegistration(String target) {
        return registries.containsKey(target);
    }

    /**
     * Returns a valid registration for the given target, if available. Returns null if no suitable
     * registration was found.
     *
     * @param target is the target of which a valid registration is requested of.
     * @return registration, if exists, otherwise null.
     */
    public Registration getRegistrationIfExists(String target) {
        DiscoveryRegistry registry = registries.get(target);
        return registry != null ? registry.getRegistration() : null;
    }

    /**
     * Adds a new registration to the registrar service.
     *
     * @param target is the target to add the registration to.
     * @param registration is the registration that is added.
     */
    public void addRegistration(String target, @Valid Registration registration) {
        if (!containsRegistration(target)) {
            registries.put(target, new DiscoveryRegistry(registryCacheEvictionTimeMinutes));
        }
        registries.get(target).addRegistration(registration);
    }

}
