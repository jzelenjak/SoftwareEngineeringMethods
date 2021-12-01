package nl.tudelft.sem.gateway.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class DiscoveryRegistryTest {

    @Test
    void testConstructor() {
        DiscoveryRegistry registry = new DiscoveryRegistry(1);
        assertNotNull(registry);
    }

    @Test
    void testGetEvictionTime() {
        DiscoveryRegistry registry = new DiscoveryRegistry(2021);
        assertEquals(2021, registry.getCacheEvictionTimeMinutes());
    }

    @Test
    void testAddSingleRegistration() {
        DiscoveryRegistry registry = new DiscoveryRegistry(1);
        Registration registration = new Registration("localhost", 1234);

        // Should not exist at the start
        assertNull(registry.getRegistration());

        // Add it, and verify that it was added (and remains added)
        registry.addRegistration(registration);
        assertThat(registry.getRegistration()).isEqualTo(registration);
        assertThat(registry.getRegistration()).isEqualTo(registration);
    }

    @Test
    void testAddMultipleRegistrations() {
        DiscoveryRegistry registry = new DiscoveryRegistry(1);
        List<Registration> registrations = List.of(new Registration("localhost", 2020),
                new Registration("tudelft.nl", 8080),
                new Registration("my.website.com", 6969));

        // Should not exist at the start
        assertNull(registry.getRegistration());

        // Add the registries, and verify that they are in the correct order (+ cyclic)
        for (var registration : registrations) {
            registry.addRegistration(registration);
        }
        registrations.forEach(
                registration -> assertThat(registration).isEqualTo(registry.getRegistration()));
        registrations.forEach(
                registration -> assertThat(registration).isEqualTo(registry.getRegistration()));
    }

    @Test
    void testAddRegistrationDirectEviction() {
        DiscoveryRegistry registry = new DiscoveryRegistry(0);
        Registration registration = new Registration("localhost", 1234);

        // Should not exist at the start
        assertNull(registry.getRegistration());

        // Add object, and watch it being ignored
        registry.addRegistration(registration);
        Thread.yield();
        assertNull(registry.getRegistration());
    }

}
