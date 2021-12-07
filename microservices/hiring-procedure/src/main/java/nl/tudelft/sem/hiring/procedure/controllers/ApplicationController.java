package nl.tudelft.sem.hiring.procedure.controllers;

import java.time.LocalDateTime;
import java.util.List;
import javax.management.InstanceNotFoundException;
import lombok.Data;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.hiring.procedure.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hiring-procedure")
@Data
public class ApplicationController {
    private final static String courseNotFound = "Course not found";
    private final static String userNotFound = "User not found";
    private final static String invalidToken = "Provided token is not valid";

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
     * @param courseId The course that the students wish to apply to.
     * @param authHeader The JWT token of the user's session.
     */
    @PostMapping("/apply")
    @ResponseBody
    public void applyTa(@RequestParam() long courseId, @RequestHeader() HttpHeaders authHeader) {
        long userId;
        String jsonParse;
        LocalDateTime courseStart;
        Boolean isStudent;
        Mono<String> courseStartMono;
        try {
            isStudent = checkStudent(authHeader);
            if (!isStudent) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User is not a student");
            }
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, invalidToken);
        }

        try {
            courseStartMono = getCourseStartDate(courseId);
            jsonParse = courseStartMono.block();
        }  catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseNotFound);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, invalidToken);
        }
        // Check if application with same credentials exists
        if (applicationService.checkSameApplication(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "User has already applied");
        }

        // Register application
        LocalDateTime now = LocalDateTime.now();
        applicationService.createApplication(userId, courseId, now);
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
            if (!checkLecturer(authHeader))
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User is not a lecturer");
        } catch (InstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, invalidToken);
        }

        try {
            getCourseStartDate(courseId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseNotFound);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, invalidToken);
        }
    }

    /**
     * Endpoint for hiring a TA. The selected student must be a candidate TA to that course and not
     * already hired. The client must be a lecturer.
     *
     * @param userId The ID of the user that is to be hired
     * @param courseId The ID of the course that the user will be hired to
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, invalidToken);
        }

        try {
            getCourseStartDate(courseId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseNotFound);
        }

        try {
            checkUserExists(userId).block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFound);
        }

        if (!applicationService.checkCandidate(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "User is not a viable candidate");
        }
        // Register hiring
        applicationService.hire(userId, courseId);
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
        if (jwtUtils.validateToken(resolvedToken)) {
            return Long.parseLong(jwtUtils.getUsername(resolvedToken));
        } else {
            throw new InstanceNotFoundException(invalidToken);
        }
    }

    /**
     * Sends a request to Courses. Gets the response and returns a Mono start date of the course
     * if successful. Otherwise, an InstanceNotFoundException is thrown.
     *
     * @param courseId The course ID received from the client
     * @return Mono of the course start date
     * @throws InstanceNotFoundException when Courses returns an error
     */
    private Mono<String> getCourseStartDate(long courseId) throws InstanceNotFoundException {
        System.out.println(gatewayConfig.getHost() + ":" + gatewayConfig.getPort());
        Mono<ClientResponse> response = webClient.get()
            .uri("http://" + gatewayConfig.getHost() + ":" + gatewayConfig.getPort()
                + "/api/courses/get-start-date?courseId=" + courseId)
            .exchange();
        return response
            .flatMap(clientResponse -> {
                if (clientResponse.statusCode().isError())
                    return Mono.error(new InstanceNotFoundException(courseNotFound));
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
        if (jwtUtils.validateToken(resolvedToken)) {
            String role = jwtUtils.getRole(resolvedToken);
            return role.equals("student");
        } else {
            throw new InstanceNotFoundException(invalidToken);
        }
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
        if (jwtUtils.validateToken(resolvedToken)) {
            String role = jwtUtils.getRole(resolvedToken);
            return role.equals("lecturer");
        } else {
            throw new InstanceNotFoundException(invalidToken);
        }
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
        Mono<ClientResponse> response = webClient.get()
            .uri("http://" + gatewayConfig.getHost() + ":" + gatewayConfig.getPort()
                + "/api/users/by_userid?user_id=" + userId)
            .exchange();
        return response
            .flatMap(clientResponse -> {
                if (clientResponse.statusCode().isError())
                    return Mono.error(new InstanceNotFoundException(userNotFound));
                return Mono.just(true);
            });
    }

}
