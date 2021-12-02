package nl.tudelft.sem.gateway.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.util.List;
import nl.tudelft.sem.gateway.discovery.Registration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
public class DiscoveryRegistrarControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    private static final String contentType = "Content-Type";
    private static final String jsonContentHeader = "application/json";

    @Test
    void testGetNonExistingTarget() throws Exception {
        mockMvc.perform(get("/discovery/non-existing"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Could not find active microservice registration for "
                        + "'non-existing' to forward request to"));
    }

    @Test
    void testSingleMicroservice() throws Exception {
        Registration toBeRegistered = new Registration("my.amazing.link", 5678);
        mockMvc.perform(post("/discovery/register/my-amazing-link")
                        .header(contentType, jsonContentHeader)
                        .content(new Gson().toJson(toBeRegistered)))
                .andExpect(status().isOk());

        // Verify that it was registered
        mockMvc.perform(get("/discovery/my-amazing-link"))
                .andExpect(status().isOk())
                .andExpect(header().string(contentType, jsonContentHeader))
                .andExpect(content().json(new Gson().toJson(toBeRegistered)));
    }

    @Test
    void testMultipleSimilarMicroservices() throws Exception {
        List<Registration> registrations = List.of(new Registration("my.website.cool", 6969),
                new Registration("www.google.com", 8080),
                new Registration("www.tudelft.nl", 1234));

        for (var registration : registrations) {
            mockMvc.perform(post("/discovery/register/multi-pool")
                            .header(contentType, jsonContentHeader)
                            .content(new Gson().toJson(registration)))
                    .andExpect(status().isOk());
        }

        // The pool should remain ordered
        for (var registration : registrations) {
            mockMvc.perform(get("/discovery/multi-pool"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(contentType, jsonContentHeader))
                    .andExpect(content().json(new Gson().toJson(registration)));
        }
    }

}
