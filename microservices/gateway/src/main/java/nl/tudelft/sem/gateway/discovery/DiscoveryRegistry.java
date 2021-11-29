package nl.tudelft.sem.gateway.discovery;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import lombok.Data;

/**
 * Class for managing registrations for a single endpoint. Uses time-based eviction methods.
 */
@Data
public class DiscoveryRegistry {

    /**
     * Class for keeping track of registered entries. Stores the registration info as well as the
     * time the item was registered.
     */
    private static class RegistryEntry {
        // The time that the registration was added
        private final long currentTime;

        // Information specific to the registration
        private final Registration registration;

        /**
         * Constructs a RegistryEntry object.
         *
         * @param registration is the registration info that is associated to this object.
         * @implNote Sets the registration time automatically during parameter initialization.
         */
        public RegistryEntry(Registration registration) {
            this.currentTime = System.currentTimeMillis();
            this.registration = registration;
        }

        /**
         * Retrieves the time that the registration was added (in milliseconds).
         *
         * @return the time that the registration was added in milliseconds.
         */
        public long getCurrentTime() {
            return currentTime;
        }

        /**
         * Retrieves the registration info associated to this entry instance.
         *
         * @return Registration information structure.
         */
        public Registration getRegistration() {
            return registration;
        }
    }

    // Time at which a cache entry is invalidated
    private static final int CACHE_EVICTION_TIME_MINUTES = 1;

    // Priority queue and cache for storing the entries in a FIFO like manner + time-based eviction
    private PriorityQueue<RegistryEntry> registrationPq;
    private final Cache<String, Registration> registrations;

    /**
     * Constructs a discovery registry instance that keeps track of all registrations for a
     * particular endpoint.
     */
    public DiscoveryRegistry() {
        this.registrationPq = new PriorityQueue<>(Comparator
                .comparingLong(RegistryEntry::getCurrentTime));

        this.registrations = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EVICTION_TIME_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Adds a new registration to the discovery registry.
     *
     * @param registration is a valid registration instance that contains information regarding the
     *                     to be registered endpoint.
     * @implNote only one thread can call this function at a time.
     */
    public synchronized void addRegistration(@Valid Registration registration) {
        registrations.put(registration.getPath(), registration);
        registrationPq.add(new RegistryEntry(registration));
    }

    /**
     * Retrieves a registration in a FIFO like manner. If no registration exists, or all
     * registrations are invalidated, null is returned.
     *
     * @return Registration object that contains information with respect to the registration.
     * @implNote only one thread can call this function at a time.
     */
    public synchronized Registration getRegistration() {
        // If cache is empty, then empty the pq as well
        if (registrations.size() == 0) {
            this.registrationPq = new PriorityQueue<>();
        }

        // Targeted registration object
        Registration target = null;
        
        // Attempt to find the service that was least recently used
        while (!registrationPq.isEmpty() && target == null) {
            RegistryEntry current = this.registrationPq.poll();
            target = registrations.getIfPresent(current.getRegistration().getPath());
        }

        // Add the successful candidate to the pq again
        this.registrationPq.add(new RegistryEntry(target));

        // Return the target object
        return target;
    }

}
