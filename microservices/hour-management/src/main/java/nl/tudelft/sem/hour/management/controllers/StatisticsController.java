package nl.tudelft.sem.hour.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.AggregationStatistics;
import nl.tudelft.sem.hour.management.dto.MultipleStatisticsRequests;
import nl.tudelft.sem.hour.management.dto.StatisticsRequest;
import nl.tudelft.sem.hour.management.dto.UserHoursStatisticsRequest;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.services.NotificationService;
import nl.tudelft.sem.hour.management.services.StatisticsService;
import nl.tudelft.sem.hour.management.validation.AsyncAuthValidator;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator;
import nl.tudelft.sem.hour.management.validation.AsyncValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hour-management/statistics")
@Data
public class StatisticsController {

    private final NotificationService notificationService;
    private final HourDeclarationRepository hourDeclarationRepository;
    private final transient GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;
    private final transient ObjectMapper objectMapper;
    private final transient StatisticsService statisticsService;

    /**
     * Retrieves the total amount of hours declared by a student for a particular course.
     *
     * @param headers           headers of the request.
     * @param statisticsRequest request containing the student id and course id.
     * @return total amount of hours declared by a student for a particular course.
     */
    @PostMapping("/total-hours")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> getTotalHours(@RequestHeader HttpHeaders headers,
                               @RequestBody StatisticsRequest statisticsRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(AsyncRoleValidator.Roles.ADMIN,
                                        AsyncRoleValidator.Roles.LECTURER,
                                        AsyncRoleValidator.Roles.STUDENT))
                ).build();

        return head.validate(headers, "").flatMap(valid -> {
            Optional<Double> totalHours = statisticsService.getTotalHoursPerStudentPerCourse(
                    statisticsRequest.getStudentId(), statisticsRequest.getCourseId());

            // Check if the student has declared hours for the course
            if (totalHours.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No statistics found for the specified student and course."));
            }

            // Return the response as json
            JsonObject result = new JsonObject();
            result.addProperty("totalHours", totalHours.get());
            return Mono.just(result.toString());
        });
    }

    /**
     * Retrieves the total amount of hours declared by listed students for listed courses.
     *
     * @param headers                    headers of the request.
     * @param userHoursStatisticsRequest requests containing student ids, course ids,
     *                                   minimum hours and amount of results.
     * @return total amount of hours declared by students for specified courses.
     */
    @PostMapping("/total-user-hours")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> getTotalHoursPerStudentPerCourse(@RequestHeader HttpHeaders headers,
                                                  @RequestBody UserHoursStatisticsRequest
                                                          userHoursStatisticsRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(AsyncRoleValidator.Roles.ADMIN,
                                        AsyncRoleValidator.Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap(valid -> {
            // Convert statistics to json object
            JsonObject jsonObject = new JsonObject();

            // Fetch the declaration statistics
            statisticsService.getTotalHoursPerStudentPerCourse(
                            userHoursStatisticsRequest.getStudentIds(),
                            userHoursStatisticsRequest.getCourseIds(),
                            userHoursStatisticsRequest.getMinHours(),
                            userHoursStatisticsRequest.getAmount())
                    .forEach(t -> jsonObject.addProperty(t.getStudentId().toString(),
                            t.getTotalHours()));

            // Check if at least one of the students declared hours for any of the courses
            if (jsonObject.size() == 0) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No statistics found for the specified course and students."));
            }

            // Return the response object
            return Mono.just(jsonObject.toString());
        });
    }

    /**
     * Retrieves the aggregation statistics for declarations with specified values.
     *
     * @param headers                    headers of the request.
     * @param multipleStatisticsRequests requests containing student ids, course ids
     * @return aggregation statistics for declarations with specified values.
     */
    @PostMapping(path = "/aggregation-stats",
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<AggregationStatistics> getAggregationStatistics(@RequestHeader HttpHeaders headers,
                                          @RequestBody MultipleStatisticsRequests
                                                             multipleStatisticsRequests) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(AsyncRoleValidator.Roles.ADMIN,
                                        AsyncRoleValidator.Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap(valid -> {
            Optional<AggregationStatistics> statistics = statisticsService
                    .calculateAggregationStatistics(multipleStatisticsRequests.getStudentIds(),
                                                    multipleStatisticsRequests.getCourseIds());

            // Check if at least one of the students declared hours for any of the courses
            if (statistics.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No statistics found for the specified course and students."));
            }

            // Return the response object
            return Mono.just(statistics.get());
        });
    }
}
