package nl.tudelft.sem.hour.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.dto.StatisticsRequest;
import nl.tudelft.sem.hour.management.dto.StudentHoursTuple;
import nl.tudelft.sem.hour.management.dto.UserHoursStatisticsRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.services.NotificationService;
import nl.tudelft.sem.hour.management.services.StatisticsService;
import nl.tudelft.sem.hour.management.validation.AsyncAuthValidator;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator;
import nl.tudelft.sem.hour.management.validation.AsyncRoleValidator.Roles;
import nl.tudelft.sem.hour.management.validation.AsyncValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hour-management")
@Data
public class HourDeclarationController {

    private final NotificationService notificationService;
    private final HourDeclarationRepository hourDeclarationRepository;
    private final transient GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;
    private final transient ObjectMapper objectMapper;
    private final transient StatisticsService statisticsService;

    /**
     * Entry point of the repo, also acts as a sanity check.
     *
     * @return a simple greeting
     */
    @GetMapping
    public @ResponseBody
    String hello() {
        return "Hello from Hour Management";
    }

    /**
     * Gets all the stored declarations in the system.
     *
     * @return all stored declaration in the system
     */
    @GetMapping("/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllDeclarations(
            @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        // Validate the request, if it succeeds, attempt to return the declarations
        return head.validate(headers, "").flatMap((valid) -> {
            List<HourDeclaration> result = hourDeclarationRepository.findAll();

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There are no declarations in the system."));
            }

            return Mono.just(result);
        });
    }

    /**
     * Allows a user to post a new hour-declaration.
     *
     * @param hourDeclarationRequest hour declaration that will be saved
     * @return an informative message about status of request
     */
    @PostMapping("/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> declareHours(@RequestHeader HttpHeaders headers,
                              @RequestBody HourDeclarationRequest hourDeclarationRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.TA))
                ).build();


        return head.validate(headers, hourDeclarationRequest.toJson()).flatMap((valid) -> {
            HourDeclaration hourDeclaration = new HourDeclaration(hourDeclarationRequest);
            long declarationId = hourDeclarationRepository.save(hourDeclaration).getDeclarationId();
            return Mono.just(String.format("Declaration with id %s has been successfully saved.",
                    declarationId));
        });
    }

    /**
     * Get a declaration associated with declarationId.
     *
     * @param declarationId id of the desired student
     * @return all declared hours associated with a student
     */
    @GetMapping("/declaration/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<HourDeclaration> getSpecifiedDeclaration(@PathVariable("id") long declarationId,
                                                  @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            Optional<HourDeclaration> result = hourDeclarationRepository.findById(declarationId);

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("There are no declarations with id: %d in the system.",
                                declarationId)));
            }

            return Mono.just(result.get());
        });

    }


    /**
     * Allows a lecturer to reject/delete an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     * @return an informative message about status of request
     */
    @DeleteMapping("/declaration/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<Void> deleteDeclaredHour(@PathVariable("id") long declarationId,
                                  @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            // Fetch the declaration from the database
            Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                    .findById(declarationId);

            // Verify that the declaration exists and has not been approved yet
            if (hourDeclaration.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration does not exists."));
            } else if (hourDeclaration.get().isApproved()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration has been approved already."));
            }

            // Remove the declaration from the database
            hourDeclarationRepository.delete(hourDeclaration.get());

            // Add a notification to the student's notification pool
            return notificationService.notify(hourDeclaration.get().getDeclarationId(),
                    String.format("Your declaration with id %s has been rejected.", declarationId),
                    headers.getFirst(HttpHeaders.AUTHORIZATION));
        });
    }

    /**
     * Allows a lecturer to approve an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     * @return an informative message about status of request
     */
    @PutMapping("/declaration/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<Void> approveDeclaredHour(@PathVariable("id") long declarationId,
                                   @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                    .findById(declarationId);

            if (hourDeclaration.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration does not exists."));
            } else if (hourDeclaration.get().isApproved()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration has been approved."));
            }

            // approve the declaration
            hourDeclaration.get().setApproved(true);
            hourDeclarationRepository.save(hourDeclaration.get());

            // Add a notification to the student's notification pool
            return notificationService.notify(hourDeclaration.get().getDeclarationId(),
                    String.format("Your declaration with id %s has been approved.", declarationId),
                    headers.getFirst(HttpHeaders.AUTHORIZATION));
        });

    }

    /**
     * Gets all unapproved declarations in the system.
     *
     * @return all stored unapproved declarations
     */
    @GetMapping("/declaration/unapproved")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllUnapprovedDeclarations(@RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            List<HourDeclaration> result = hourDeclarationRepository.findByApproved(false);

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "There are no declarations in the system."));
            }

            return Mono.just(result);
        });
    }

    /**
     * Gets all declarations associated with a student.
     *
     * @param studentId id of the desired student
     * @return all declared hours associated with a student
     */
    @GetMapping("/declaration/student/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllDeclarationsByStudent(@PathVariable("id") long studentId,
                                                            @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            List<HourDeclaration> result = hourDeclarationRepository.findByStudentId(studentId);

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("There are no declarations by student: %d in the system.",
                                studentId)));
            }

            return Mono.just(result);
        });
    }

    /**
     * Retrieves the total amount of hours declared by a student for a particular course.
     *
     * @param headers           headers of the request.
     * @param statisticsRequest request containing the student id and course id.
     * @return total amount of hours declared by a student for a particular course.
     */
    @GetMapping("/declaration/statistics/total-hours")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> getTotalHours(@RequestHeader HttpHeaders headers,
                               @RequestBody StatisticsRequest statisticsRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER, Roles.TA))
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
    @PostMapping("/statistics/total-user-hours")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> getTotalHoursPerStudentPerCourse(@RequestHeader HttpHeaders headers,
                                                  @RequestBody UserHoursStatisticsRequest
                                                          userHoursStatisticsRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER))
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

}
