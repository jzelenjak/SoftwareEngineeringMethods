package nl.tudelft.sem.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.gateway.discovery.Registration;
import nl.tudelft.sem.gateway.service.DiscoveryRegistrarService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
public class GatewayControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    private static MockWebServer mockWebServer;

    @Autowired
    private transient DiscoveryRegistrarService discoveryRegistrarService;

    private static final String contentType = "Content-Type";
    private static final String jsonContentHeader = "application/json";

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testDefaultGatewayResponse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from Gateway!"));
    }

    @Test
    void testGatewayNoRegistration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGatewayWithRegistrationValidResponse() throws Exception {
        // Register listener
        String target = "single-registration";
        HttpUrl url = mockWebServer.url("/api/" + target);
        discoveryRegistrarService.addRegistration(target, new Registration(url.host(), url.port()));

        // Enqueue request to mock server
        mockWebServer.enqueue(new MockResponse()
                .addHeader(contentType, jsonContentHeader)
                .setBody("Hello test!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get("/api/single-registration"))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello test!"))
                .andExpect(content().contentType(jsonContentHeader));

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testGatewayWithRegistrationInValidResponse() throws Exception {
        // Register listener
        String target = "single-invalid-response";
        HttpUrl url = mockWebServer.url("/api/" + target);
        discoveryRegistrarService.addRegistration(target, new Registration(url.host(), url.port()));

        // Enqueue request to mock server
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader(contentType, jsonContentHeader)
                .setBody("No! This is bad!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get("/api/single-registration"))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No! This is bad!"))
                .andExpect(content().contentType(jsonContentHeader));

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("GET", recordedRequest.getMethod());
    }

}
