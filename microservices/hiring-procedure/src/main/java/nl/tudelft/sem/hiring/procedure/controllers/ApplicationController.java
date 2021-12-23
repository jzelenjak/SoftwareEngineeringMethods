package nl.tudelft.sem.hiring.procedure.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import lombok.Data;
import lombok.val;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.services.NotificationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncAuthValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseExistsValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseTimeValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator.Roles;
import nl.tudelft.sem.hiring.procedure.validation.AsyncTaLimitValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncUserExistsValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncValidator;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hiring-procedure")
@Data
public class ApplicationController {
    private static final String COURSE_NOT_FOUND = "Course not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_TOKEN = "Provided token is not valid";

    private ApplicationService applicationService;
    private final NotificationService notificationService;
    private CourseInfoResponseCache courseInfoCache;
    private WebClient webClient;
    private GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;

    /**
     * Constructor for the Application Controller.
     *
     * @param applicationService      Specifies the ApplicationService.
     * @param notificationService     the notification service.
     * @param courseInfoCache         the course info response cache.
     * @param jwtUtils                the jwt utils.
     * @param gatewayConfig           the gateway config.
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService,
                                 NotificationService notificationService,
                                 CourseInfoResponseCache courseInfoCache, JwtUtils jwtUtils,
                                 GatewayConfig gatewayConfig) {
        this.applicationService = applicationService;
        this.notificationService = notificationService;
        this.courseInfoCache = courseInfoCache;
        this.webClient = WebClient.create();
        this.jwtUtils = jwtUtils;
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Endpoint for students to apply for a TA position on a specific course.
     *
     * @param courseId   The course that the students wish to apply to.
     * @param authHeader The JWT token of the user's session.
     */
    @PostMapping("/apply")
    @ResponseBody
    public Mono<Void> applyTa(@RequestParam() long courseId,
                              @RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT, Roles.TA)),
                        new AsyncCourseTimeValidator(courseInfoCache, courseId))
                .build();

        return head.validate(authHeader, "").flatMap(value -> {
            long userId;
            userId = getUserIdFromToken(authHeader);

            if (applicationService.checkSameApplication(userId, courseId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User has already applied"));
            }

            applicationService.createApplication(userId, courseId, LocalDateTime.now());
            return Mono.empty();
        });
    }

    /**
     * Endpoint for retrieving all applications for a specific course.
     * User must be a lecturer.
     *
     * @param courseId The ID of the course
     * @return A list of all applications that have been found
     */
    @GetMapping("/get-applications")
    @ResponseBody
    public Mono<List<Application>> getApplications(@RequestParam() long courseId,
                                                   @RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)))
                .build();

        return head.validate(authHeader, "").flatMap(value ->
                Mono.just(applicationService.getApplicationsForCourse(courseId)));


    }

    /**
     * Endpoint for retrieving all applications.
     * User must be a lecturer.
     *
     * @return A list of all applications that have been found
     */
    @GetMapping("/get-all-applications")
    @ResponseBody
    public Mono<List<Application>> getAllApplications(@RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)))
                .build();

        return head.validate(authHeader, "").flatMap(value ->
                Mono.just(applicationService.getAllApplications()));
    }

    /**
     * Endpoint for hiring a TA. The selected student must be a candidate TA to that course and not
     * already hired. The client must be a lecturer.
     *
     * @param userId     The ID of the user that is to be hired
     * @param courseId   The ID of the course that the user will be hired to
     * @param authHeader The JWT token of the client's session.
     */
    @PostMapping("/hire-TA")
    @ResponseBody
    public Mono<Void> hireTa(@RequestParam() long userId, @RequestParam() long courseId,
                             @RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)),
                        new AsyncCourseExistsValidator(courseInfoCache, courseId),
                        new AsyncUserExistsValidator(gatewayConfig, userId),
                        new AsyncTaLimitValidator(applicationService, courseInfoCache, courseId)
                ).build();

        return head.validate(authHeader, "").flatMap(value -> {
            if (!applicationService.checkCandidate(userId, courseId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User is not a viable candidate"));
            }
            Optional<Application> optionalApplication =
                    applicationService.getApplication(userId, courseId);
            if (optionalApplication.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
                        "Application with userId %s and courseId %s has not been found.",
                                userId, courseId)));
            }
            Application application = optionalApplication.get();
            // Register hiring
            applicationService.hire(userId, courseId);

            // Send notification request
            notificationService.notify(userId,
                    String.format("Your application with id %s has been approved.",
                            application.getApplicationId()),
                    authHeader.getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.empty();
        });
    }

    /**
     * Updates the status of an application to be rejected.
     *
     * @param applicationId id of the application to be rejected.
     * @param headers       the headers of the request.
     */
    @PostMapping("reject")
    public Mono<Void> reject(@RequestParam long applicationId, @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN))
                ).build();

        // Perform validation, and reject application if exists
        return head.validate(headers, "").flatMap(value -> {
            // Fetch the application using the provided ID
            Optional<Application> applicationOpt = applicationService.getApplication(applicationId);

            // Check if the application exists
            if (applicationOpt.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Application not found"));
            }

            // Check if the application is already processed (withdrawn, rejected, or accepted)
            Application application = applicationOpt.get();
            if (application.getStatus() != ApplicationStatus.IN_PROGRESS) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Application has already been processed"));
            }

            // Change the status of the application to rejected
            applicationService.rejectApplication(applicationId);

            // Send notification request
            notificationService.notify(application.getUserId(),
                    String.format("Your application with id %s has been rejected.",
                            application.getApplicationId()),
                    headers.getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.empty();
        });
    }

    /**
     * Allows students to withdraw their application.
     *
     * @param courseId is the ID of the course that was applied to.
     * @param headers  is the list of request headers.
     */
    @PostMapping("withdraw")
    public Mono<Void> withdraw(@RequestParam long courseId, @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils,
                                Set.of(Roles.STUDENT, Roles.TA, Roles.ADMIN)),
                        new AsyncCourseTimeValidator(courseInfoCache, courseId)
                ).build();

        // Perform validation, and map to an appropriate response
        return head.validate(headers, "").flatMap(value -> {
            String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
            String resolvedToken = jwtUtils.resolveToken(token);

            long userId = jwtUtils.getUserId(jwtUtils.validateAndParseClaims(resolvedToken));
            Optional<Application> application = applicationService.getApplication(userId, courseId);

            // Check if the application is valid (exists)
            if (application.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Application does not exist"));
            }

            // Check if the application has not been processed yet
            if (application.get().getStatus() != ApplicationStatus.IN_PROGRESS) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Application has already been processed"));
            }

            // Change the status of the application to 'withdrawn'
            applicationService.withdrawApplication(application.get().getApplicationId());
            return Mono.empty();
        });
    }

    /**
     * Endpoint for retrieving the contract of a user, for a course.
     * Current implementation just returns a template contract, to be filled by the TA.
     *
     * @param userId   is the ID of the user for which the contract is requested.
     *                 Parameter may be null, in which case the contract was requested
     *                 by the userId in the JWT
     * @param courseId is the ID of the course for which the contract is requested
     * @param headers  is the list of the request headers
     * @return a byte stream of the contract pdf
     */
    @GetMapping("get-contract")
    public Mono<byte[]> getContract(@RequestParam(required = false) Long userId,
                                    @RequestParam long courseId,
                                    @RequestHeader HttpHeaders headers) {
        AsyncValidator.Builder builder = AsyncValidator.Builder.newBuilder();
        builder.addValidator(new AsyncAuthValidator(jwtUtils));
        if (userId != null) {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                    Set.of(Roles.LECTURER, Roles.ADMIN)));
            AsyncValidator head = builder.build();
            return head.validate(headers, "").flatMap(value -> {
                try {
                    return Mono.just(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("templatecontract.pdf").readAllBytes());
                } catch (IOException e) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Resource was not found"));
                }
            });
        } else {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                    Set.of(Roles.STUDENT, Roles.TA)));
            AsyncValidator head = builder.build();
            return head.validate(headers, "").flatMap(value -> {
                try {
                    byte[] contract = Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("templatecontract.pdf").readAllBytes();
                    return Mono.just(contract);
                } catch (IOException e) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Resource was not found"));
                }
            });
        }
    }

    /**
     * Endpoint for fetching the maximum contractually allowed hours of work for an application.
     *
     * @param userId The ID of the user that is associated to that application.
     *               If the user is a student, this is not specified.
     * @param courseId The ID of the course that is associated to that application
     * @param headers The headers of the request. Should contain the JWT.
     * @return The max allowed hours
     */
    @GetMapping("get-max-hours")
    public Mono<Integer> getMaxHours(@RequestParam(required = false) Long userId,
                           @RequestParam long courseId,
                           @RequestHeader HttpHeaders headers) {
        AsyncValidator head = buildVariableValidator(userId);
        return head.validate(headers, "").flatMap(value -> {
            Long finalUserId = userId;
            if (userId == null) {
                finalUserId = getUserIdFromToken(headers);
            }
            try {
                return Mono.just(applicationService.getMaxHours(finalUserId, courseId));
            } catch (NoSuchElementException e) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no submission that is associated to that userId and courseId"));
            }
        });
    }

    /**
     * Endpoint for updating the value of the maximum allowed hours for an application.
     *
     * @param applicationId The id of the application for which to update the value.
     * @param headers The headers of the request. Should contain the JWT.
     * @param body The body of the request. Should contain the specified maxHours.
     * @return 200 OK if request goes through, or errors if anything goes wrong.
     */
    @PostMapping("set-max-hours")
    public Mono<Void> setMaxHours(@RequestParam long applicationId,
                                  @RequestHeader HttpHeaders headers, @RequestBody String body) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN))
                ).build();
        return head.validate(headers, "").flatMap(value -> {
            int hours;
            Gson gson = new Gson();
            JsonElement hoursJsonObject = gson.fromJson(body, JsonObject.class).get("maxHours");
            if (hoursJsonObject == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Body was not configured accordingly. Please see documentation"));
            }
            hours = hoursJsonObject.getAsInt();
            try {
                applicationService.setMaxHours(applicationId, hours);
            } catch (NoSuchElementException e) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no submission that is associated to that userId and courseId"));
            }
            return Mono.empty();
        });
    }

    /**
     * Endpoint for getting the rating of a TA for a course.
     *
     * @param userId The ID of the user that is associated to the requested application
     * @param courseId The ID of the course that the TA is working on
     * @param headers The headers of the request. Should contain the JWT.
     * @return The rating of the TA, or errors if anything goes wrong. Check docs
     */
    @GetMapping("get-rating")
    public Mono<Double> getRating(@RequestParam(required = false) Long userId,
                                  @RequestParam long courseId,
                                  @RequestHeader HttpHeaders headers) {
        AsyncValidator head = buildVariableValidator(userId);

        return head.validate(headers, "").flatMap(value -> {
            Long finalUserId = userId;
            if (userId == null) {
                finalUserId = getUserIdFromToken(headers);
            }
            try {
                return Mono.just(applicationService.getRating(finalUserId, courseId));
            } catch (Exception e) {
                if (e.getClass().equals(NoSuchElementException.class)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "There is no submission that is associated to that "
                                    + "userId and courseId"));
                }
                if (e.getMessage().equals("Application is not approved.")) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "The respective application has not been approved"));
                }
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The respective TA has no rating for this course"));
            }
        });
    }

    /**
     * Endpoint for updating the value of the maximum allowed hours for an application.
     *
     * @param applicationId The id of the application for which to update the value.
     * @param headers The headers of the request. Should contain the JWT.
     * @param body The body of the request. Should contain the specified maxHours.
     * @return 200 OK if request goes through, or errors if anything goes wrong.
     */
    @PostMapping("rate")
    public Mono<Void> setRating(@RequestParam long applicationId,
                                  @RequestHeader HttpHeaders headers, @RequestBody String body) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN))
                ).build();

        return head.validate(headers, "").flatMap(value -> {
            double rating;
            Gson gson = new Gson();
            JsonElement ratingJsonObject = gson.fromJson(body, JsonObject.class).get("rating");
            if (ratingJsonObject == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Body was not configured accordingly. Please see documentation"));
            }
            rating = ratingJsonObject.getAsDouble();
            if (rating < 0.0 || rating > 10.0) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rating should be between 0 and 10."));
            }
            try {
                applicationService.setRating(applicationId, rating);
            } catch (Exception e) {
                if (e.getClass().equals(NoSuchElementException.class)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "There is no submission that is associated to that "
                                    + "userId and courseId"));
                }
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The respective application has not been approved"));
            }
            return Mono.empty();
        });
    }

    /**
     * Checks if the JWT is valid. If it is, returns the userId.
     *
     * @param authJwt The authentication header received from the client
     * @return The userId
     */
    private long getUserIdFromToken(HttpHeaders authJwt) {
        String resolvedToken = jwtUtils.resolveToken(authJwt.getFirst("Authorization"));
        Jws<Claims> userClaims = jwtUtils.validateAndParseClaims(resolvedToken);
        return jwtUtils.getUserId(userClaims);
    }

    private AsyncValidator buildVariableValidator(Long userId) {
        AsyncValidator.Builder builder = AsyncValidator.Builder.newBuilder();
        builder.addValidator(new AsyncAuthValidator(jwtUtils));
        if (userId != null) {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                    Set.of(Roles.LECTURER, Roles.ADMIN)));

        } else {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                    Set.of(Roles.TA)));
        }
        return builder.build();
    }

}
