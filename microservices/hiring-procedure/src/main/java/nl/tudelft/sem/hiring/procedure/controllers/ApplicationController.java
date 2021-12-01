package nl.tudelft.sem.hiring.procedure.controllers;

import java.time.LocalDateTime;
import javax.management.InstanceNotFoundException;
import lombok.Data;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import nl.tudelft.sem.hiring.procedure.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hiring-procedure")
@Data
public class ApplicationController {
    private ApplicationService applicationService;
    private WebClient webClient;
    private final transient JwtUtils jwtUtils;

    /**
     * Constructor for the Application Controller.
     *
     * @param applicationService Specifies the ApplicationService
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService, JwtUtils jwtUtils) {
        this.applicationService = applicationService;
        this.webClient = WebClient.create();
        this.jwtUtils = jwtUtils;
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
        LocalDateTime courseStart;
        Boolean isStudent;
        Mono<LocalDateTime> courseStartMono;
        // Get course start date
        try {
            isStudent = checkStudent(authHeader);
            if (!isStudent) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User is not a student");
            }
            courseStartMono = getCourseStartDate(courseId);
            courseStart = courseStartMono.block();
            // Check if student is within deadline
            if (!applicationService.checkDeadline(courseStart)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "Deadline has passed");
            }
            userId = checkJwt(authHeader);
            // Check if application with same credentials exists
            if (applicationService.checkSameApplication(userId, courseId)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "User has already applied");
            }

            // Register application
            applicationService.createApplication(userId, courseId);
        } catch (InstanceNotFoundException e) {
            if (e.getMessage().equals("That course does not exist.")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not valid");
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
        String resolvedToken = jwtUtils.resolveToken(authJwt.toString());
        if (jwtUtils.validateToken(resolvedToken)) {
            return Long.parseLong(jwtUtils.getUsername(resolvedToken));
        } else {
            throw new InstanceNotFoundException("The provided token is invalid");
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
    private Mono<LocalDateTime> getCourseStartDate(long courseId) throws InstanceNotFoundException {
        Mono<ClientResponse> response = webClient.post()
            .uri("http:localhost:8080/api/courses/get-start-date?courseId=" + courseId)
            .exchange();
        try {
            return response
                .flatMap(clientResponse -> clientResponse.bodyToMono(LocalDateTime.class));
        } catch (WebClientException e) {
            throw new InstanceNotFoundException("That course does not exist.");
        }
    }

    /**
     * Function for checking if the  user has student permission. It does so by resolving the JWT.
     *
     * @param authJwt The authentication header received from the client
     * @return True if the user is a student, false otherwise
     * @throws InstanceNotFoundException when the JWT is invalid
     */
    private Boolean checkStudent(HttpHeaders authJwt) throws InstanceNotFoundException {
        String resolvedToken = jwtUtils.resolveToken(authJwt.toString());
        if (jwtUtils.validateToken(resolvedToken)) {
            String role = jwtUtils.getRole(resolvedToken);
            return role.equals("student");
        } else {
            throw new InstanceNotFoundException("The provided token is invalid");
        }
    }
}
