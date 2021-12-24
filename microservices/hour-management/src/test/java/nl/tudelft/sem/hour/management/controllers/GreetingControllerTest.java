package nl.tudelft.sem.hour.management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    @Test
    void testGreeting() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management"))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat("Hello from Hour Management").isEqualTo(actualResponseBody);
    }

    @Test
    void testTeapot() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/coffee"))
                .andExpect(status().isIAmATeapot())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat("Congratulations, you get a nice cup of Earl Gray!")
                .isEqualTo(actualResponseBody);
    }
}
