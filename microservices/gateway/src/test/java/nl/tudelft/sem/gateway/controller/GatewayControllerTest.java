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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GatewayControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    private transient MockWebServer mockWebServer;

    @Autowired
    private transient DiscoveryRegistrarService discoveryRegistrarService;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
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
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("Hello test!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get("/api/" + target))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello test!"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

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
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("No! This is bad!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get("/api/" + target))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No! This is bad!"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("GET", recordedRequest.getMethod());
    }

}
