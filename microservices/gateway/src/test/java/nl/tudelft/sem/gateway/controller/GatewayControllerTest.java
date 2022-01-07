package nl.tudelft.sem.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.gateway.discovery.Registration;
import nl.tudelft.sem.gateway.exceptions.MonoForwardingException;
import nl.tudelft.sem.gateway.service.DiscoveryRegistrarService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import reactor.core.publisher.Mono;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GatewayControllerTest {

    private static final String API_PREFIX = "/api/";
    private static final String authorizationToken = "myToken";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient GatewayController gatewayController;

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
        HttpUrl url = mockWebServer.url(API_PREFIX + target);
        discoveryRegistrarService.addRegistration(target, new Registration(url.host(), url.port()));

        // Enqueue request to mock server
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("Hello test!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get(API_PREFIX + target)
                        .header(HttpHeaders.AUTHORIZATION, authorizationToken))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello test!"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals(authorizationToken, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testGatewayWithRegistrationInValidResponse() throws Exception {
        // Register listener
        String target = "single-invalid-response";
        HttpUrl url = mockWebServer.url(API_PREFIX + target);
        discoveryRegistrarService.addRegistration(target, new Registration(url.host(), url.port()));

        // Enqueue request to mock server
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("No! This is bad!"));

        // Perform call to registered listener
        MvcResult result = mockMvc.perform(get(API_PREFIX + target)
                        .header(HttpHeaders.AUTHORIZATION, authorizationToken))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No! This is bad!"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals(authorizationToken, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testGatewayWithRegistrationErrorThrown() throws Exception {
        // Register listener
        String target = "invalid-forwarded-exception";
        HttpUrl url = mockWebServer.url(API_PREFIX + target);
        discoveryRegistrarService.addRegistration(target, new Registration(url.host(), url.port()));

        // Enqueue request to mock server
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("No! This is bad!"));

        // Setup request mock object
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        when(requestMock.getMethod()).thenReturn(HttpMethod.GET.name());
        when(requestMock.getScheme()).thenReturn("http");
        when(requestMock.getRequestURI()).thenReturn(url.toString());
        when(requestMock.getQueryString()).thenReturn("");
        when(requestMock.getServerName()).thenReturn(url.host());
        when(requestMock.getServerPort()).thenReturn(url.port());

        // Perform manual call to check the Mono response
        // This should be a forwarded exception (MonoForwardingException)
        Mono<ResponseEntity<String>> response = gatewayController
                .getRequest(target, "", new HttpHeaders(), requestMock);

        // Verify that the mono contains a forwarded exception
        // Should contain no response (resolved)
        assertNull(response.onErrorResume(throwable -> {
            assertEquals(throwable.getClass(), MonoForwardingException.class);
            return Mono.empty();
        }).block());

        // Perform additional verification
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
    }

}
