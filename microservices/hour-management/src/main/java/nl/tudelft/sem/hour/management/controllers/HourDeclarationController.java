package nl.tudelft.sem.hour.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import nl.tudelft.sem.hour.management.config.GatewayConfig;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import nl.tudelft.sem.hour.management.services.NotificationService;
import nl.tudelft.sem.hour.management.services.StatisticsService;
import nl.tudelft.sem.hour.management.validation.AsyncAuthValidator;
import nl.tudelft.sem.hour.management.validation.AsyncCourseTimeValidator;
import nl.tudelft.sem.hour.management.validation.AsyncDeclarationValidator;
import nl.tudelft.sem.hour.management.validation.AsyncHiringValidator;
import nl.tudelft.sem.hour.management.validation.AsyncLecturerValidator;
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
@RequestMapping("/api/hour-management/declaration")
@Data
public class HourDeclarationController {

    private final NotificationService notificationService;
    private final HourDeclarationRepository hourDeclarationRepository;
    private final transient GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;
    private final transient ObjectMapper objectMapper;
    private final transient StatisticsService statisticsService;

    /**
     * Gets all the stored declarations in the system.
     *
     * @return all stored declaration in the system
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllDeclarations(
            @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils, Set.of(Roles.ADMIN))
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
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<String> declareHours(@RequestHeader HttpHeaders headers,
                              @RequestBody HourDeclarationRequest hourDeclarationRequest) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.STUDENT)),
                        new AsyncCourseTimeValidator(gatewayConfig),
                        new AsyncHiringValidator(gatewayConfig, jwtUtils),
                        new AsyncDeclarationValidator(jwtUtils, hourDeclarationRequest)
                ).build();

        return head.validate(headers, hourDeclarationRequest.toJson()).flatMap((valid) -> {
            HourDeclaration hourDeclaration = new HourDeclaration(hourDeclarationRequest);
            long declarationId = hourDeclarationRepository.save(hourDeclaration).getDeclarationId();
            return createInfoResponse(
                    String.format("Declaration with id %s has been successfully saved.",
                            declarationId));
        });
    }

    /**
     * Get a declaration associated with declarationId.
     *
     * @param declarationId id of the desired student
     * @return all declared hours associated with a student
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<HourDeclaration> getSpecifiedDeclaration(@PathVariable("id") long declarationId,
                                                  @RequestHeader HttpHeaders headers) {
        // Fetch the info for a single declaration
        Optional<HourDeclaration> result = hourDeclarationRepository.findById(declarationId);

        // Verify that the declaration exists
        if (result.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("There are no declarations with id: %d in the system.",
                            declarationId)));
        }

        // Construct validator chain, and return result only if the user is authorized
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER),
                                result.get().getStudentId())
                ).build();

        return head.validate(headers, "").flatMap(valid -> Mono.just(result.get()));
    }


    /**
     * Allows a lecturer to reject/delete an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     * @return an informative message about status of request
     */
    @DeleteMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<Void> deleteDeclaredHour(@PathVariable("id") long declarationId,
                                  @RequestHeader HttpHeaders headers) {

        // Fetch the declaration from the database
        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(declarationId);

        // Verify that the declaration exists
        if (hourDeclaration.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Given declaration does not exists."));
        }

        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER)),
                        new AsyncLecturerValidator(gatewayConfig, jwtUtils,
                                hourDeclaration.get().getCourseId())
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            // Verify that has not been approved yet
            if (hourDeclaration.get().isApproved()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration has been approved already."));
            }

            // Remove the declaration from the database
            hourDeclarationRepository.delete(hourDeclaration.get());

            // Add a notification to the student's notification pool
            return notificationService.notify(hourDeclaration.get().getStudentId(),
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
    @PutMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<Void> approveDeclaredHour(@PathVariable("id") long declarationId,
                                   @RequestHeader HttpHeaders headers) {

        // Fetch the declaration from the database
        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(declarationId);

        // Verify that the declaration exists
        if (hourDeclaration.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Given declaration does not exists."));
        }

        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER)),
                        new AsyncLecturerValidator(gatewayConfig, jwtUtils,
                                hourDeclaration.get().getCourseId())
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            // Verify that has not been approved yet
            if (hourDeclaration.get().isApproved()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Given declaration has been approved."));
            }

            // approve the declaration
            hourDeclaration.get().setApproved(true);
            hourDeclarationRepository.save(hourDeclaration.get());

            // Add a notification to the student's notification pool
            return notificationService.notify(hourDeclaration.get().getStudentId(),
                    String.format("Your declaration with id %s has been approved.", declarationId),
                    headers.getFirst(HttpHeaders.AUTHORIZATION));
        });

    }

    /**
     * Gets all unapproved declarations in the system.
     *
     * @return all stored unapproved declarations.
     */
    @GetMapping("/unapproved")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllUnapprovedDeclarations(@RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils, Set.of(Roles.ADMIN))
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            List<HourDeclaration> result = hourDeclarationRepository.findByApproved(false);

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "There are no unapproved declarations in the system."));
            }

            return Mono.just(result);
        });
    }

    /**
     * Gets all unapproved declarations in the system.
     *
     * @param courseId is the id of the course to fetch the unapproved declarations for.
     * @return all stored unapproved declarations.
     */
    @GetMapping("/unapproved/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllUnapprovedDeclarationsForCourse(
            @RequestHeader HttpHeaders headers,
            @PathVariable("courseId") long courseId) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER)),
                        new AsyncLecturerValidator(gatewayConfig, jwtUtils, courseId)
                ).build();

        return head.validate(headers, "").flatMap((valid) -> {
            List<HourDeclaration> result = hourDeclarationRepository
                    .findByCourseIdAndApproved(courseId, false);

            if (result.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("There are no unapproved declarations for course with id %d.",
                                courseId)));
            }

            return Mono.just(result);
        });
    }

    /**
     * Gets all declarations associated with a student.
     *
     * @param studentId id of the desired student.
     * @return all declared hours associated with a student.
     */
    @GetMapping("/student/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<List<HourDeclaration>> getAllDeclarationsByStudent(@PathVariable("id") long studentId,
                                                            @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(gatewayConfig, jwtUtils),
                        new AsyncRoleValidator(gatewayConfig, jwtUtils,
                                Set.of(Roles.ADMIN, Roles.LECTURER), studentId)
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
     * Creates an informative response in JSON format.
     *
     * @param message is the message to return.
     * @return a JSON response with the given message.
     */
    private Mono<String> createInfoResponse(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        return Mono.just(jsonObject.toString());
    }

}
