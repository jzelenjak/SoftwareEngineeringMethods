package nl.tudelft.sem.hiring.procedure.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import lombok.Data;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.validation.AsyncAuthValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseExistsValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseTimeValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator.Roles;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hiring-procedure")
@Data
public class ApplicationController {
    private static final String COURSE_NOT_FOUND = "Course not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_TOKEN = "Provided token is not valid";

    private ApplicationService applicationService;
    private WebClient webClient;
    private GatewayConfig gatewayConfig;
    private final transient JwtUtils jwtUtils;

    /**
     * Constructor for the Application Controller.
     *
     * @param applicationService Specifies the ApplicationService
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService, JwtUtils jwtUtils,
                                 GatewayConfig gatewayConfig) {
        this.applicationService = applicationService;
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
                new AsyncCourseTimeValidator(gatewayConfig, courseId))
            .build();

        return head.validate(authHeader, "").flatMap(value -> {
            long userId;
            try {
                userId = checkJwt(authHeader);
            } catch (InstanceNotFoundException e) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN));
            }

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
                new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER)))
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
                new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER)))
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
                new AsyncRoleValidator(jwtUtils, Set.of(Roles.LECTURER)),
                new AsyncCourseExistsValidator(gatewayConfig, courseId),
                new AsyncUserExistsValidator(gatewayConfig, userId))
            .build();

        return head.validate(authHeader, "").flatMap(value -> {
            if (!applicationService.checkCandidate(userId, courseId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User is not a viable candidate"));
            }
            // Register hiring
            applicationService.hire(userId, courseId);
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
                        new AsyncCourseTimeValidator(gatewayConfig, courseId)
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

            // Remove the application
            applicationService.removeApplication(application.get());
            return Mono.empty();
        });
    }

    /**
     * Endpoint for retrieving the contract of a user, for a course.
     * Current implementation just returns a template contract, to be filled by the TA.
     *
     * @param userId is the ID of the user for which the contract is requested.
     *               Parameter may be null, in which case the contract was requested
     *               by the userId in the JWT
     * @param courseId is the ID of the course for which the contract is requested
     * @param headers is the list of the request headers
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
                        .getResourceAsStream("contracttemplate.md").readAllBytes());
                } catch (IOException e) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resource was not found"));
                }
            });
        } else {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                Set.of(Roles.STUDENT)));
            AsyncValidator head = builder.build();
            return head.validate(headers, "").flatMap(value -> {
                try {
                    byte[] contract = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("contracttemplate.md").readAllBytes();
                    return Mono.just(contract);
                } catch (IOException e) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resource was not found"));
                }
            });
        }
    }

    /**
     * Checks if the JWT is valid. If it is, returns the userId.
     *
     * @param authJwt The authentication header received from the client
     * @return The userId
     * @throws InstanceNotFoundException when the JWT is invalid
     */
    private long checkJwt(HttpHeaders authJwt) throws InstanceNotFoundException {
        String resolvedToken = jwtUtils.resolveToken(authJwt.getFirst("Authorization"));
        Jws<Claims> userClaims = jwtUtils.validateAndParseClaims(resolvedToken);
        if (userClaims != null) {
            return jwtUtils.getUserId(userClaims);
        }
        throw new InstanceNotFoundException(INVALID_TOKEN);
    }

}
