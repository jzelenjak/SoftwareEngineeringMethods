package nl.tudelft.sem.hiring.procedure.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.services.NotificationService;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncAuthValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseCandidacyValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseExistsValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseTimeValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncLecturerValidator;
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
public class SubmissionController {
    private static final String COURSE_NOT_FOUND = "Course not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_TOKEN = "Provided token is not valid";

    private SubmissionService submissionService;
    private final NotificationService notificationService;
    private CourseInfoResponseCache courseInfoCache;
    private WebClient webClient;
    private GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;

    /**
     * Constructor for the Submission Controller.
     *
     * @param submissionService   Specifies the SubmissionService.
     * @param notificationService the notification service.
     * @param courseInfoCache     the course info response cache.
     * @param jwtUtils            the jwt utils.
     * @param gatewayConfig       the gateway config.
     */
    @Autowired
    public SubmissionController(SubmissionService submissionService,
                                NotificationService notificationService,
                                CourseInfoResponseCache courseInfoCache, JwtUtils jwtUtils,
                                GatewayConfig gatewayConfig) {
        this.submissionService = submissionService;
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
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT)),
                        new AsyncCourseTimeValidator(courseInfoCache, courseId),
                        new AsyncCourseCandidacyValidator(jwtUtils, submissionService,
                                gatewayConfig, courseId))
                .build();

        return head.validate(authHeader, "").flatMap(value -> {
            long userId = getUserIdFromToken(authHeader);

            if (!submissionService.checkSameSubmission(userId, courseId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User has already applied"));
            }

            submissionService.createSubmission(userId, courseId, LocalDateTime.now());
            return Mono.empty();
        });
    }

    /**
     * Endpoint for retrieving all submissions for a specific course.
     * User must be a lecturer.
     *
     * @param courseId The ID of the course
     * @return A list of all submissions that have been found
     */
    @GetMapping("/get-submissions")
    @ResponseBody
    public Mono<List<Submission>> getSubmissions(@RequestParam() long courseId,
                                                 @RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)),
                        new AsyncLecturerValidator(jwtUtils, gatewayConfig, courseId))
                .build();

        return head.validate(authHeader, "").flatMap(value ->
                Mono.just(submissionService.getSubmissionsForCourse(courseId)));


    }

    /**
     * Endpoint for retrieving all submissions.
     * User must be a lecturer.
     *
     * @return A list of all submissions that have been found
     */
    @GetMapping("/get-all-submissions")
    @ResponseBody
    public Mono<List<Submission>> getAllSubmissions(@RequestHeader() HttpHeaders authHeader) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)))
                .build();

        return head.validate(authHeader, "").flatMap(value ->
                Mono.just(submissionService.getAllSubmissions()));
    }

    /**
     * Endpoint for hiring a TA. The selected student must be a candidate TA to that course and not
     * already hired. The client must be a lecturer.
     *
     * @param submissionId id of the submission to be hired
     * @param authHeader   The JWT token of the client's session.
     */
    @PostMapping("/hire-TA")
    @ResponseBody
    public Mono<Void> hireTa(@RequestParam long submissionId,
                             @RequestHeader HttpHeaders authHeader) {
        // Fetch the submission using the provided ID
        Optional<Submission> submissionOpt = submissionService.getSubmission(submissionId);

        // Check if the submission exists
        if (submissionOpt.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Submission not found"));
        }
        Submission submission = submissionOpt.get();
        long courseId = submission.getCourseId();

        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)),
                        new AsyncLecturerValidator(jwtUtils, gatewayConfig, courseId),
                        new AsyncCourseExistsValidator(courseInfoCache, courseId),
                        new AsyncUserExistsValidator(gatewayConfig, submission.getUserId()),
                        new AsyncTaLimitValidator(submissionService, courseInfoCache, courseId)
                ).build();

        return head.validate(authHeader, "").flatMap(value -> {
            if (!submissionService.checkCandidate(submissionId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User is not a viable candidate"));
            }

            // Register hiring
            submissionService.hire(submission.getSubmissionId());

            // Send notification request
            notificationService.notify(submission.getUserId(),
                    String.format("Your submission with id %s has been approved.",
                            submission.getSubmissionId()),
                    authHeader.getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.empty();
        });
    }

    /**
     * Updates the status of a submission to be rejected.
     *
     * @param submissionId id of the submission to be rejected.
     * @param headers      the headers of the request.
     */
    @PostMapping("reject")
    public Mono<Void> reject(@RequestParam long submissionId, @RequestHeader HttpHeaders headers) {
        // Fetch the submission using the provided ID
        Optional<Submission> submissionOpt = submissionService.getSubmission(submissionId);

        // Check if the submission exists
        if (submissionOpt.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Submission not found"));
        }
        Submission submission = submissionOpt.get();

        // Construct validator chain
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER, Roles.ADMIN)),
                        new AsyncLecturerValidator(jwtUtils, gatewayConfig,
                                submission.getCourseId())
                ).build();

        // Perform validation, and reject submission if exists
        return head.validate(headers, "").flatMap(value -> {
            // Check if the submission is already processed (withdrawn, rejected, or accepted)
            if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Submission has already been processed"));
            }

            // Change the status of the submission to rejected
            submissionService.rejectSubmission(submissionId);

            // Send notification request
            notificationService.notify(submission.getUserId(),
                    String.format("Your submission with id %s has been rejected.",
                            submission.getSubmissionId()),
                    headers.getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.empty();
        });
    }

    /**
     * Allows students to withdraw their submission.
     *
     * @param courseId is the ID of the course that was applied to.
     * @param headers  is the list of request headers.
     */
    @PostMapping("withdraw")
    public Mono<Void> withdraw(@RequestParam long courseId, @RequestHeader HttpHeaders headers) {
        AsyncValidator head = AsyncValidator.Builder.newBuilder()
                .addValidators(
                        new AsyncAuthValidator(jwtUtils),
                        new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT, Roles.ADMIN)),
                        new AsyncCourseTimeValidator(courseInfoCache, courseId)
                ).build();

        // Perform validation, and map to an appropriate response
        return head.validate(headers, "").flatMap(value -> {
            String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
            String resolvedToken = jwtUtils.resolveToken(token);

            long userId = jwtUtils.getUserId(jwtUtils.validateAndParseClaims(resolvedToken));
            Optional<Submission> submission = submissionService.getSubmission(userId, courseId);

            // Check if the submission is valid (exists)
            if (submission.isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Submission does not exist"));
            }

            // Check if the submission has not been processed yet
            if (submission.get().getStatus() != SubmissionStatus.IN_PROGRESS) {
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "Submission has already been processed"));
            }

            // Change the status of the submission to 'withdrawn'
            submissionService.withdrawSubmission(submission.get().getSubmissionId());
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
            builder.addValidator(new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT)));
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
     * Endpoint for fetching the maximum contractually allowed hours of work for an submission.
     *
     * @param userId   The ID of the user that is associated to that submission.
     *                 If the user is a student, this is not specified.
     * @param courseId The ID of the course that is associated to that submission
     * @param headers  The headers of the request. Should contain the JWT.
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
                System.out.println(finalUserId + " " + courseId);
                return Mono.just(submissionService.getMaxHours(finalUserId, courseId));
            } catch (NoSuchElementException e) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no submission that is associated to that userId and courseId"));
            }
        });
    }

    /**
     * Endpoint for updating the value of the maximum allowed hours for an submission.
     *
     * @param submissionId The id of the submission for which to update the value.
     * @param headers      The headers of the request. Should contain the JWT.
     * @param body         The body of the request. Should contain the specified maxHours.
     * @return 200 OK if request goes through, or errors if anything goes wrong.
     */
    @PostMapping("set-max-hours")
    public Mono<Void> setMaxHours(@RequestParam long submissionId,
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
                submissionService.setMaxHours(submissionId, hours);
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
     * @param userId   The ID of the user that is associated to the requested submission
     * @param courseId The ID of the course that the TA is working on
     * @param headers  The headers of the request. Should contain the JWT.
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
                return Mono.just(submissionService.getRating(finalUserId, courseId));
            } catch (Exception e) {
                if (e.getClass().equals(NoSuchElementException.class)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "There is no submission that is associated to that "
                                    + "userId and courseId"));
                }
                if (e.getMessage().equals("Submission is not approved.")) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "The respective submission has not been approved"));
                }
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The respective student has no rating for this course"));
            }
        });
    }

    /**
     * Endpoint for updating the value of the maximum allowed hours for an submission.
     *
     * @param submissionId The id of the submission for which to update the value.
     * @param headers      The headers of the request. Should contain the JWT.
     * @param body         The body of the request. Should contain the specified maxHours.
     * @return 200 OK if request goes through, or errors if anything goes wrong.
     */
    @PostMapping("rate")
    public Mono<Void> setRating(@RequestParam long submissionId,
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
                submissionService.setRating(submissionId, rating);
            } catch (Exception e) {
                if (e.getClass().equals(NoSuchElementException.class)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "There is no submission that is associated to that "
                                    + "userId and courseId"));
                }
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "The respective submission has not been approved"));
            }
            return Mono.empty();
        });
    }

    /**
     * Endpoint for retrieving all the submissions of a student.
     *
     * @param userId  The ID of the user for which to fetch the submissions.
     *                If the request was made by a student, this should be empty,
     *                since their userId will be extracted from the JWT.
     * @param headers The headers of the request. Should contain the JWT.
     * @return A list of all submissions for that student, or errors if anything goes wrong.
     */
    @GetMapping("get-student")
    public Mono<List<Submission>> getStudentSubmissions(
            @RequestParam(required = false) Long userId,
            @RequestHeader HttpHeaders headers) {
        AsyncValidator head = buildVariableValidator(userId);

        return head.validate(headers, "").flatMap(value -> {
            Long finalUserId = userId;
            if (userId == null) {
                finalUserId = getUserIdFromToken(headers);
            }
            List<Submission> submissions = submissionService
                    .getSubmissionsForStudent(finalUserId);
            if (submissions.size() == 0) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There are no submissions for that student"));
            }
            return Mono.just(submissions);
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
            builder.addValidator(new AsyncRoleValidator(jwtUtils, Set.of(Roles.STUDENT)));
        }
        return builder.build();
    }

}
