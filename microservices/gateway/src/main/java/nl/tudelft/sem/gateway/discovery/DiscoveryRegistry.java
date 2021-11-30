package nl.tudelft.sem.gateway.discovery;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import lombok.Data;

/**
 * Class for managing registrations for a single endpoint. Uses time-based eviction methods.
 */
@Data
public class DiscoveryRegistry {

    // Time at which a cache entry is invalidated
    private static final int CACHE_EVICTION_TIME_MINUTES = 1;

    // Queue and cache for storing the entries in a FIFO like manner + time-based eviction
    private Queue<Registration> registrationQueue;
    private final Cache<String, Registration> registrations;

    /**
     * Constructs a discovery registry instance that keeps track of all registrations for a
     * particular endpoint.
     */
    public DiscoveryRegistry() {
        this.registrationQueue = new LinkedList<>();
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
    public synchronized void addRegistration(@Valid Registration registration)
            throws ExecutionException {
        // Updates the entry in case of a heartbeat. If entry does not exist, load it into the queue
        // and add it to the cache
        registrations.get(registration.remoteAddress(), () -> {
            registrationQueue.add(registration);
            return registration;
        });
    }

    /**
     * Retrieves a registration in a FIFO like manner. If no registration exists, or all
     * registrations are invalidated, null is returned.
     *
     * @return Registration object that contains information with respect to the registration.
     * @implNote only one thread can call this function at a time.
     */
    public synchronized Registration getRegistration() {
        // If cache is empty, then empty the pq as well to avoid doing extra work
        if (registrations.size() == 0) {
            registrationQueue = new PriorityQueue<>();
            return null;
        }

        // Targeted registration object
        Registration target = null;

        // Attempt to find the service that was least recently used
        while (!registrationQueue.isEmpty() && target == null) {
            Registration current = registrationQueue.poll();
            target = registrations.getIfPresent(current.remoteAddress());
        }

        // If not null, add the successful candidate to the queue again and let the cache know to
        // postpone invalidation
        if (target != null) {
            registrations.put(target.remoteAddress(), target);
            registrationQueue.add(target);
        }

        // Return the target object
        return target;
    }

}
