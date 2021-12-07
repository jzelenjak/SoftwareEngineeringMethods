package nl.tudelft.sem.hour.management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class HourDeclarationControllerTest {

    private static final String declarationPath = "/api/hour-management/declaration";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    private transient JavaType listHourType;

    private final transient LocalDateTime testDate = LocalDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(1234, 5678, 1);
    private final transient  HourDeclarationRequest hourDeclarationRequestSameStudent =
            new HourDeclarationRequest(1234, 567812, 12);
    private final transient HourDeclarationRequest hourDeclarationRequestNew =
            new HourDeclarationRequest(12345, 567812, 12);

    private final transient HourDeclaration hourDeclarationUnapproved = new HourDeclaration(1,
            hourDeclarationRequest, false, testDate);
    private final transient HourDeclaration hourDeclarationApproved = new HourDeclaration(2,
            hourDeclarationRequestNew, true, testDate);

    @BeforeEach
    void init() {
        hourDeclarationRepository.deleteAll();

        listHourType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, HourDeclaration.class);
        hourDeclarationRepository.save(hourDeclarationUnapproved);
        hourDeclarationRepository.save(hourDeclarationApproved);
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

        MvcResult mvcResult = mockMvc.perform(get(declarationPath))
                .andExpect(status().isOk())
                .andReturn();

        List<HourDeclaration> actualResponseBody = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), listHourType);


        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void testGetAllDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        mockMvc.perform(get(declarationPath))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void testPostDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(declarationPath)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(hourDeclarationRequestNew)))
            .andExpect(status().isOk())
            .andReturn();

        Optional<HourDeclaration> saved = hourDeclarationRepository.findById(3L);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(saved.isEmpty()).isFalse();
        assertThat(saved.get().getStudentId()).isEqualTo(hourDeclarationRequestNew.getStudentId());
        assertThat(saved.get().getCourseId()).isEqualTo(hourDeclarationRequestNew.getCourseId());
        assertThat(saved.get().getCourseId()).isEqualTo(hourDeclarationRequestNew.getCourseId());

        assertThat(actualResponseBody)
                .isEqualTo("Declaration with id 3 has been successfully saved.");
    }

    @Test
    void testPostDeclarationInvalid() throws Exception {
        mockMvc.perform(post(declarationPath)
                .contentType("application/json")
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSpecifiedDeclaration() throws Exception {
        Optional<HourDeclaration> expectedResponseBody = hourDeclarationRepository.findById(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/1"))
                .andExpect(status().isOk())
                .andReturn();

        HourDeclaration actualResponseBody = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), HourDeclaration.class);

        assertThat(expectedResponseBody.isEmpty()).isFalse();
        assertThat(actualResponseBody).isEqualTo(expectedResponseBody.get());
    }

    @Test
    void testGetSpecifiedDeclarationInvalidId() throws Exception {
        hourDeclarationRepository.deleteAll();

        mockMvc.perform(get("/api/hour-management/declaration/9999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/api/hour-management/declaration/1/reject"))
                .andExpect(status().isOk())
                .andReturn();

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        // ensures that delete is no longer in system
        assertThat(hourDeclaration.isEmpty()).isTrue();

        assertThat(actualResponseBody)
                .isEqualTo("Declaration with id 1 has been successfully deleted.");
    }

    @Test
    void testDeleteDeclarationInvalidId() throws Exception {
        mockMvc.perform(delete("/api/hour-management/declaration/20/reject"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteDeclarationApproved() throws Exception {
        mockMvc.perform(delete("/api/hour-management/declaration/2/reject"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApproveDeclaration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/hour-management/declaration/1/approve")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(1L);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(hourDeclaration.isEmpty()).isFalse();
        assertThat(hourDeclaration.get().isApproved()).isTrue();

        assertThat(actualResponseBody)
                .isEqualTo("Declaration with id 1 has been successfully approved.");

    }

    @Test
    void testApproveDeclarationInvalidId() throws Exception {
        mockMvc.perform(put("/api/hour-management/declaration/20/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApproveDeclarationApproved() throws Exception {
        mockMvc.perform(put("/api/hour-management/declaration/2/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUnapproved() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByApproved(false);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/unapproved"))
                .andExpect(status().isOk())
                .andReturn();

        List<HourDeclaration> actualResponseBody = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), listHourType);

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void testGetAllUnapprovedDeclarationsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        mockMvc.perform(get("/api/hour-management/declaration/unapproved"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void testGetAllDeclarationsByStudent() throws Exception {
        List<HourDeclaration> expectedResponseBody = hourDeclarationRepository
                .findByStudentId(1234);

        MvcResult mvcResult = mockMvc.perform(get("/api/hour-management/declaration/student/1234"))
                .andExpect(status().isOk())
                .andReturn();

        List<HourDeclaration> actualResponseBody = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), listHourType);

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void testGetAllDeclarationsByStudentEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        mockMvc.perform(get("/api/hour-management/declaration/student/9999"))
                .andExpect(status().isBadRequest());
    }
}
