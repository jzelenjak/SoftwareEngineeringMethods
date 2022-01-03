package nl.tudelft.sem.gateway.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DiscoveryRegistryTest {

    @Mock
    private transient Cache<String, Registration> cacheMock;

    @BeforeEach
    private void setupEach() {
        // Create fresh mocks
        MockitoAnnotations.initMocks(this);
    }

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

    @Test
    void testGetRegistrationReRegistrations() {
        LinkedList<Registration> registrations = new LinkedList<>();
        Registration registration = new Registration("localhost", 1234);
        registrations.add(registration);

        // Create the registry using the test objects
        DiscoveryRegistry registry = new DiscoveryRegistry(registrations, cacheMock);

        // Configure behaviour of mock
        when(cacheMock.size()).thenReturn(1L);
        when(cacheMock.getIfPresent(registration.remoteAddress())).thenReturn(registration);

        // Add it, and verify that it was added (and remains added)
        registry.addRegistration(registration);
        assertThat(registry.getRegistration()).isEqualTo(registration);

        // Assert that there is no eviction time due to the custom cache
        assertThat(registry.getCacheEvictionTimeMinutes()).isEqualTo(-1);

        // Verify that the registration got re-registered once by the getRegistration method
        verify(cacheMock, times(1)).put(registration.remoteAddress(), registration);
    }

}
