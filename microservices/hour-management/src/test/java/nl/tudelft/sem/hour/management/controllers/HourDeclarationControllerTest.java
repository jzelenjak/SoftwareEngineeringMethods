package nl.tudelft.sem.hour.management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.dto.StatisticsRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class HourDeclarationControllerTest {

    private static final String declarationPath = "/api/hour-management/declaration";

    private static final String authorization = "Authorization";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    private final transient LocalDateTime testDate = LocalDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(1234, 5678, 1);
    private final transient HourDeclarationRequest hourDeclarationRequestSameStudent =
            new HourDeclarationRequest(1234, 5678, 15);
    private final transient HourDeclarationRequest hourDeclarationRequestNew =
            new HourDeclarationRequest(12345, 567812, 12);

    private final transient HourDeclaration hourDeclarationUnapproved = new HourDeclaration(1,
            hourDeclarationRequest, false, testDate);
    private final transient HourDeclaration hourDeclarationApproved = new HourDeclaration(2,
            hourDeclarationRequestNew, true, testDate);
    private final transient HourDeclaration hourDeclarationSameStudent = new HourDeclaration(3,
            hourDeclarationRequestSameStudent, false, testDate);

    @BeforeEach
    void init() {
        hourDeclarationRepository.deleteAll();

        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);

        when(jwtUtils.resolveToken(Mockito.any())).thenReturn("");
        when(jwtUtils.validateAndParseClaims(Mockito.any())).thenReturn(jwsMock);
        when(jwtUtils.getRole(Mockito.any())).thenReturn(AsyncRoleValidator.Roles.ADMIN.name());
    }

    @Test
    void testGreeting() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management"))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat("Hello from Hour Management").isEqualTo(actualResponseBody);
    }

    @Test
    void testGetAllDeclarations() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findAll();

        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        MvcResult mvcResult = mockMvc.perform(get(declarationPath)
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(hourDeclarationRequestNew))
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("Declaration with id 3 has been successfully saved."));

        Optional<HourDeclaration> saved = hourDeclarationRepository.findById(3L);

        assertThat(saved.isEmpty()).isFalse();
        assertThat(saved.get().getStudentId()).isEqualTo(hourDeclarationRequestNew.getStudentId());
        assertThat(saved.get().getCourseId()).isEqualTo(hourDeclarationRequestNew.getCourseId());
    }

    @Test
    void testPostDeclarationInvalid() throws Exception {
        mockMvc.perform(post(declarationPath)
                        .contentType("application/json")
                        .content("")
                        .header(authorization, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSpecifiedDeclaration() throws Exception {
        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findById(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/1")
                        .header(authorization, ""))
                .andExpect(status().isOk())
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetSpecifiedDeclarationInvalidId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/9999")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/1/reject")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Declaration with id 1 has been successfully deleted."));

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);

        // ensures that delete is no longer in system
        assertThat(hourDeclaration.isEmpty()).isTrue();
    }

    @Test
    void testDeleteDeclarationInvalidId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/20/reject")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteDeclarationApproved() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/2/reject")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApproveDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/1/approve")
                        .contentType("application/json")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Declaration with id 1 has been successfully approved."));

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);

        assertThat(hourDeclaration.isEmpty()).isFalse();
        assertThat(hourDeclaration.get().isApproved()).isTrue();
    }

    @Test
    void testApproveDeclarationInvalidId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/20/approve")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApproveDeclarationApproved() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/2/approve")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUnapproved() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByApproved(false);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllUnapprovedDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeclarationsByStudent() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(1234);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/1234")
                        .header(authorization, ""))
                .andReturn();

        // Wait for response
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponseBody)));
    }

    @Test
    void testGetAllDeclarationsByStudentEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/9999")
                        .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTotalHours() throws Exception {
        hourDeclarationRepository.save(hourDeclarationSameStudent);

        Optional<Double> expectedTotalHours =
                hourDeclarationRepository.aggregateHoursFor(1234L, 5678L);

        StatisticsRequest statisticsRequest = new StatisticsRequest(1234L, 5678L);

        MvcResult mvcResult = mockMvc.perform(
                get("/api/hour-management/declaration/statistics/total-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statisticsRequest))
                        .header(authorization, ""))
                .andReturn();

        assertThat(expectedTotalHours.isEmpty()).isFalse();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(String.format(Locale.ROOT,
                                "{\"totalHours\": %f}", expectedTotalHours.get())));
    }

    @Test
    void testGetTotalHoursNotFound() throws Exception {
        StatisticsRequest statisticsRequest = new StatisticsRequest(9999L, 5678L);

        MvcResult mvcResult = mockMvc.perform(
                        get("/api/hour-management/declaration/statistics/total-hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statisticsRequest))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }
}
