package nl.tudelft.sem.hiring.procedure.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import nl.tudelft.sem.hiring.procedure.validation.AsyncCourseTimeValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator;
import nl.tudelft.sem.hiring.procedure.validation.AsyncRoleValidator.Roles;
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
    public Mono<Void> applyTa(@RequestParam() long courseId, @RequestHeader() HttpHeaders authHeader) {
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
                return Mono.error(new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User has already applied"));
            }

            applicationService.createApplication(userId, courseId, LocalDateTime.now());
            return Mono.empty();
        });


        /*long userId;
        String jsonParse;
        Boolean isStudent;
        try {
            isStudent = checkStudent(authHeader);
            if (!isStudent) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "User is not a student");
            }
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN);
        }

        LocalDateTime courseStart;
        Mono<String> courseStartMono;
        try {
            courseStartMono = getCourseStartDate(courseId);
            jsonParse = courseStartMono.block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND);
        }

        jsonParse = jsonParse.split("\"")[3];
        courseStart = LocalDateTime.parse(jsonParse);
        // Check if student is within deadline
        if (!applicationService.checkDeadline(courseStart)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "Deadline has passed");
        }
        try {
            userId = checkJwt(authHeader);
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN);
        }
        // Check if application with same credentials exists
        if (applicationService.checkSameApplication(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User has already applied");
        }

        // Register application
        LocalDateTime now = LocalDateTime.now();
        applicationService.createApplication(userId, courseId, now);*/
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
    public List<Application> getApplications(@RequestParam() long courseId,
                                             @RequestHeader() HttpHeaders authHeader) {
        try {
            if (!checkLecturer(authHeader)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "User is not a lecturer");
            }
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN);
        }

        try {
            getCourseStartDate(courseId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND);
        }
        return applicationService.getApplicationsForCourse(courseId);
    }

    /**
     * Endpoint for retrieving all applications.
     * User must be a lecturer.
     *
     * @return A list of all applications that have been found
     */
    @GetMapping("/get-all-applications")
    @ResponseBody
    public List<Application> getAllApplications(@RequestHeader() HttpHeaders authHeader) {
        try {
            if (!checkLecturer(authHeader)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "User is not a lecturer");
            }
            return applicationService.getAllApplications();
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN);
        }
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
    public void hireTa(@RequestParam() long userId, @RequestParam() long courseId,
                       @RequestHeader() HttpHeaders authHeader) {
        boolean isLecturer;
        try {
            isLecturer = checkLecturer(authHeader);
            if (!isLecturer) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                        "User is not a lecturer");
            }
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, INVALID_TOKEN);
        }

        try {
            getCourseStartDate(courseId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND);
        }

        try {
            checkUserExists(userId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }

        if (!applicationService.checkCandidate(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User is not a viable candidate");
        }
        // Register hiring
        applicationService.hire(userId, courseId);
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
     * Checks if the JWT is valid. If it is, returns the userId.
     *
     * @param authJwt The authentication header received from the client
     * @return The userId
     * @throws InstanceNotFoundException when the JWT is invalid
     */
    private long checkJwt(HttpHeaders authJwt) throws InstanceNotFoundException {
        String resolvedToken = jwtUtils.resolveToken(authJwt.get("Authorization").get(0));
        Jws<Claims> userClaims = jwtUtils.validateAndParseClaims(resolvedToken);
        if (userClaims != null) {
            return jwtUtils.getUserId(userClaims);
        }
        throw new InstanceNotFoundException(INVALID_TOKEN);
    }

    /**
     * Sends a request to Courses. Gets the response and returns a Mono start date of the course
     * if successful. Otherwise, an InstanceNotFoundException is thrown.
     *
     * @param courseId The course ID received from the client
     * @return Mono of the course start date
     * @throws InstanceNotFoundException when Courses returns an error
     */
    private Mono<String> getCourseStartDate(long courseId) {
        System.out.println(gatewayConfig.getHost() + ":" + gatewayConfig.getPort());
        String uri = UriComponentsBuilder.newInstance().scheme("http")
                .host(gatewayConfig.getHost())
                .port(gatewayConfig.getPort())
                .pathSegment("api", "courses", "get-start-date")
                .queryParam("courseId", courseId)
                .toUriString();
        Mono<ClientResponse> response = webClient.get().uri(uri)
                .exchange();
        return response
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new InstanceNotFoundException(COURSE_NOT_FOUND));
                    }
                    return clientResponse.bodyToMono(String.class);
                });
    }

    /**
     * Function for checking if the  user has student permission. It does so by resolving the JWT.
     *
     * @param authJwt The authentication header received from the client
     * @return True if the user is a student, false otherwise
     * @throws InstanceNotFoundException when the JWT is invalid
     */
    private Boolean checkStudent(HttpHeaders authJwt) throws InstanceNotFoundException {
        String resolvedToken = jwtUtils.resolveToken(authJwt.get("Authorization").get(0));
        Jws<Claims> userClaims = jwtUtils.validateAndParseClaims(resolvedToken);
        if (userClaims != null) {
            return jwtUtils.getRole(userClaims).equals("student");
        }
        throw new InstanceNotFoundException(INVALID_TOKEN);
    }

    /**
     * Function for checking if the  user has lecturer permission. It does so by resolving the JWT.
     *
     * @param authJwt The authentication header received from the client
     * @return True if the user is a lecturer, false otherwise
     * @throws InstanceNotFoundException when the JWT is invalid
     */
    private Boolean checkLecturer(HttpHeaders authJwt) throws InstanceNotFoundException {
        String resolvedToken = jwtUtils.resolveToken(authJwt.get("Authorization").get(0));
        Jws<Claims> userClaims = jwtUtils.validateAndParseClaims(resolvedToken);
        if (userClaims != null) {
            return jwtUtils.getRole(userClaims).equals("lecturer");
        }
        throw new InstanceNotFoundException(INVALID_TOKEN);
    }

    /**
     * Sends a request to Users. Gets the response and returns a Mono regarding whether the user
     * exists or not. If  not successful, an InstanceNotFoundException is thrown.
     *
     * @param userId The user ID received from the Authentication microservice
     * @return true
     * @throws InstanceNotFoundException when Users returns an error
     */
    private Mono<Boolean> checkUserExists(long userId) throws InstanceNotFoundException {
        String uri = UriComponentsBuilder.newInstance().scheme("http")
                .host(gatewayConfig.getHost())
                .port(gatewayConfig.getPort())
                .pathSegment("api", "users", "by-userid")
                .queryParam("userId", userId)
                .toUriString();
        Mono<ClientResponse> response = webClient.get().uri(uri)
                .exchange();
        return response
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new InstanceNotFoundException(USER_NOT_FOUND));
                    }
                    return Mono.just(true);
                });
    }

}
