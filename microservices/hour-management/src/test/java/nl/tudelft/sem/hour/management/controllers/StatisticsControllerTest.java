package nl.tudelft.sem.hour.management.controllers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import nl.tudelft.sem.hour.management.dto.AggregationStatistics;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.dto.MultipleStatisticsRequests;
import nl.tudelft.sem.hour.management.dto.StatisticsRequest;
import nl.tudelft.sem.hour.management.dto.StudentHoursTuple;
import nl.tudelft.sem.hour.management.dto.UserHoursStatisticsRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.services.StatisticsService;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerTest {

    private static final String authorization = "Authorization";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Autowired
    private transient StatisticsService statisticsService;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    @MockBean
    private transient JwtUtils jwtUtils;

    @Mock
    private transient Jws<Claims> jwsMock;

    private final transient ZonedDateTime testDate = ZonedDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequest =
            new HourDeclarationRequest(1234, 5678, 1, "de");
    private final transient HourDeclarationRequest hourDeclarationRequestSameStudent =
            new HourDeclarationRequest(1234, 567812, 12, "nl");
    private final transient HourDeclarationRequest hourDeclarationRequestNew =
            new HourDeclarationRequest(12345, 567812, 1337.5, "tr");
    private final transient HourDeclarationRequest hourDeclarationRequestInvalid =
            new HourDeclarationRequest(12345, 567812, -9999, "gb");


    private final transient HourDeclaration hourDeclarationUnapproved = new HourDeclaration(1,
            hourDeclarationRequest, false, testDate);
    private final transient HourDeclaration hourDeclarationApproved = new HourDeclaration(2,
            hourDeclarationRequestNew, true, testDate);
    private final transient HourDeclaration hourDeclarationSameStudent = new HourDeclaration(3,
            hourDeclarationRequestSameStudent, false, testDate);
    private final transient HourDeclaration hourDeclarationInvalid = new HourDeclaration(4,
            hourDeclarationRequestInvalid, false, testDate);

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
    void testGetTotalHours() throws Exception {
        hourDeclarationRepository.save(hourDeclarationSameStudent);

        Optional<Double> expectedTotalHours =
                hourDeclarationRepository.aggregateHoursFor(1234L, 5678L);

        StatisticsRequest statisticsRequest = new StatisticsRequest(1234L, 5678L);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/total-hours")
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
    void testGetTotalHoursInvalidStudentId() throws Exception {
        StatisticsRequest statisticsRequest = new StatisticsRequest(9999L, 5678L);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/total-hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statisticsRequest))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTotalHoursInvalidCourseId() throws Exception {
        StatisticsRequest statisticsRequest = new StatisticsRequest(9999L, 5678L);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/total-hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statisticsRequest))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTotalHoursPerStudentPerCourse() throws Exception {
        Collection<StudentHoursTuple> expectedTotalHours = hourDeclarationRepository
                .aggregateByCourseIdSetAndStudentIdSet(Set.of(12345L), Set.of(567812L), 1.0);

        UserHoursStatisticsRequest userHoursStatisticsRequest
                = new UserHoursStatisticsRequest(1, 1.0, Set.of(12345L), Set.of(567812L));

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/total-user-hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        userHoursStatisticsRequest))
                                .header(authorization, ""))
                .andReturn();

        assertThat(expectedTotalHours.isEmpty()).isFalse();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(String.format(Locale.ROOT,
                                "{\"12345\": %f}", hourDeclarationRequestNew.getDeclaredHours())));
    }

    // User parameterized test to avoid code duplication
    @ParameterizedTest
    @MethodSource("provideRequestsForGetTotalHoursPerStudentPerCourse")
    void testGetTotalHoursPerStudentPerCourse(
            UserHoursStatisticsRequest userHoursStatisticsRequest) throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/total-user-hours")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        userHoursStatisticsRequest))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> provideRequestsForGetTotalHoursPerStudentPerCourse() {
        return Stream.of(
                Arguments.of(new UserHoursStatisticsRequest(1, 1.0, Set.of(12345L), Set.of())),
                Arguments.of(new UserHoursStatisticsRequest(1, 1.0, Set.of(), Set.of(567812L))),
                Arguments.of(new UserHoursStatisticsRequest(1, 9999.0, Set.of(), Set.of(12345L))),
                Arguments.of(new UserHoursStatisticsRequest(0, 1.0, Set.of(), Set.of(12345L)))
        );
    }

    @Test
    void testGetAggregationStatisticsValid() throws Exception {
        hourDeclarationRepository.save(hourDeclarationSameStudent);

        MultipleStatisticsRequests multipleStatisticsRequests
                = new MultipleStatisticsRequests(Set.of(1234L, 12345L), Set.of(5678L, 567812L));

        MvcResult mvcResult = mockMvc.perform(
                post("/api/hour-management/statistics/aggregation-stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                multipleStatisticsRequests))
                        .header(authorization, ""))
                .andReturn();

        Optional<AggregationStatistics> expect = statisticsService
                .calculateAggregationStatistics(multipleStatisticsRequests.getStudentIds(),
                        multipleStatisticsRequests.getCourseIds());

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        expect)));
    }

    @Test
    void testGetAggregationStatisticsInvalid() throws Exception {
        hourDeclarationRepository.save(hourDeclarationSameStudent);
        hourDeclarationRepository.save(hourDeclarationInvalid);

        MultipleStatisticsRequests multipleStatisticsRequests
                = new MultipleStatisticsRequests(Set.of(1234L, 12345L), Set.of(5678L, 567812L));

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/aggregation-stats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        multipleStatisticsRequests))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAggregationStatisticsEmpty() throws Exception {
        hourDeclarationRepository.deleteAll();

        MultipleStatisticsRequests multipleStatisticsRequests
                = new MultipleStatisticsRequests(Set.of(1234L, 12345L), Set.of(5678L, 567812L));

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/hour-management/statistics/aggregation-stats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        multipleStatisticsRequests))
                                .header(authorization, ""))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isNotFound());
    }
}
