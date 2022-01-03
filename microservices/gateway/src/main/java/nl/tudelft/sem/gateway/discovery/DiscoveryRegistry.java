package nl.tudelft.sem.gateway.discovery;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Class for managing registrations for a single endpoint. Uses time-based eviction methods.
 */
public class DiscoveryRegistry {

    // Time at which a cache entry is invalidated (-1 if unused)
    @Getter
    private final int cacheEvictionTimeMinutes;

    // Queue and cache for storing the entries in a FIFO like manner + time-based eviction
    private transient Queue<Registration> registrationQueue;
    private final transient Cache<String, Registration> registrations;

    /**
     * Constructs a discovery registry instance that keeps track of all registrations for a
     * particular endpoint.
     *
     * @param cacheEvictionTimeMinutes The time in minutes after which a registration is removed.
     */
    public DiscoveryRegistry(int cacheEvictionTimeMinutes) {
        this.cacheEvictionTimeMinutes = cacheEvictionTimeMinutes;
        this.registrationQueue = new LinkedList<>();
        this.registrations = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheEvictionTimeMinutes, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Constructs a discovery registry instance that keeps track of all registrations for a
     * particular endpoint.
     *
     * @param registrationQueue The queue to use for storing the registrations.
     * @param registrations     Custom cache for storing entities.
     * @implNote sets the cacheEvictionTimeMinutes to -1, as it is not used in this case
     *         (covered by the cache).
     */
    public DiscoveryRegistry(Queue<Registration> registrationQueue,
                             Cache<String, Registration> registrations) {
        this.cacheEvictionTimeMinutes = -1;
        this.registrationQueue = registrationQueue;
        this.registrations = registrations;
    }

    /**
     * Adds a new registration to the discovery registry.
     *
     * @param registration is a valid registration instance that contains information regarding the
     *                     to be registered endpoint.
     * @implNote only one thread can call this function at a time.
     */
    @SneakyThrows
    public synchronized void addRegistration(@Valid Registration registration) {
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
            registrationQueue = new LinkedList<>();
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
