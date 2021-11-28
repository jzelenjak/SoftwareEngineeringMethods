package nl.tudelft.sem.gateway.discovery;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

@Data
public class DiscoveryRegistry {

    private static class RegistryEntry{
        long currentTime;
        Registration registration;

        public RegistryEntry(Registration registration) {
            this.currentTime = System.currentTimeMillis();
            this.registration = registration;
        }

        public long getCurrentTime() {
            return currentTime;
        }

        public Registration getRegistration() {
            return registration;
        }
    }

    private PriorityQueue<RegistryEntry> registrationPq;
    private final Cache<String, Registration> registrations;

    public DiscoveryRegistry() {
        this.registrationPq = new PriorityQueue<>(Comparator.comparingLong(RegistryEntry::getCurrentTime));

        this.registrations = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public synchronized void addRegistration(@Valid Registration registration) {
        registrations.put(registration.getPath(), registration);
        registrationPq.add(new RegistryEntry(registration));
    }

    public synchronized String getRegistration() {
        RegistryEntry current = null;
        Registration target = null;

        // If cache is empty, then empty the pq as well
        if (registrations.size() == 0) {
            this.registrationPq = new PriorityQueue<>();
        }
        
        // Attempt to find the service that was least recently used
        while (!registrationPq.isEmpty() && target == null) {
            current = this.registrationPq.poll();
            target = registrations.getIfPresent(current.getRegistration().getPath());
        }

        // Add the successful candidate to the pq again
        this.registrationPq.add(new RegistryEntry(target));

        // Return the target path
        return target != null ? target.getPath() : null;
    }

}
