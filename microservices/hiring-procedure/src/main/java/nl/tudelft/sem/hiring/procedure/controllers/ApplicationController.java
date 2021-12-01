package nl.tudelft.sem.hiring.procedure.controllers;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javax.management.InstanceNotFoundException;
import lombok.Data;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
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

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
        this.webClient = WebClient.create();
    }

    /**
     * Endpoint for students to apply for a TA position on a specific course.
     *
     * @param courseId The course that the students wish to apply to.
     * @param authHeader The JWT token of the user's session.
     */
    @PostMapping("/apply")
    @ResponseBody
    public void applyTA(@RequestParam() long courseId, @RequestHeader() HttpHeaders authHeader) {
        long userId;
        LocalDateTime courseStart;
        Mono<Long> userIdMono;
        Mono<LocalDateTime> courseStartMono;

        // Check JWT
        try {
            userIdMono = checkJWT(authHeader);
        }
        catch (InstanceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Get course start date
        try {
            courseStartMono = getCourseStartDate(courseId);
        }
        catch (InstanceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Block all async processes
        userId = userIdMono.block();
        courseStart = courseStartMono.block();

        // Check if user is a student
        try {
            checkStudent(userId);
        }
        catch (InstanceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "User is not a student");
        }

        // Check if application with same credentials exists
        if (applicationService.checkSameApplication(userId, courseId))
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "User has already applied");

        // Check if student is within deadline
        if (!applicationService.checkDeadline(courseStart))
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Deadline has passed");

        // Register application
        applicationService.createApplication(userId, courseId);
    }

    /**
     * Sends a request to Authentication. Gets the response and returns a Mono of the userId
     * if successful. Otherwise, an InstanceNotFoundException is thrown.
     *
     * @param authJWT The authentication header received from the client
     * @return Mono of the userId
     * @throws InstanceNotFoundException when Authentication returns an error
     */
    private Mono<Long> checkJWT(HttpHeaders authJWT) throws InstanceNotFoundException {
        Mono<ClientResponse> response = webClient.post()
            .uri("http:localhost:8080/api/authentication/check")
            .headers(authJWT::addAll)
            .exchange();
        try {
            return response.flatMap(clientResponse -> clientResponse.bodyToMono(Long.class));
        }
        catch(WebClientException e) {
            throw new InstanceNotFoundException("No user is associated to that token.");
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
            .uri("http:localhost:8080/api/courses/get-start-date?courseId="+courseId)
            .exchange();
        try {
            return response
                .flatMap(clientResponse -> clientResponse.bodyToMono(LocalDateTime.class));
        }
        catch(WebClientException e) {
            throw new InstanceNotFoundException("That course does not exist.");
        }
    }

    /**
     * Sends a request to Users. Gets the response and returns a Mono regarding whether the user is
     * a student or not. If  not successful, an InstanceNotFoundException is thrown.
     *
     * @param userId The user ID received from the Authentication microservice
     * @return Mono boolean of the result
     * @throws InstanceNotFoundException when Users returns an error
     */
    private Mono<Boolean> checkStudent(long userId) throws InstanceNotFoundException {
        Mono<ClientResponse> response = webClient.post()
            .uri("http:localhost:8080/api/users/get-permission?userId="+userId)
            .exchange();
        try {
            return response.flatMap(clientResponse -> {
                return Mono.just(clientResponse.bodyToMono(String.class)
                    .equals(Mono.just("student")));
            });
        }
        catch(WebClientException e) {
            throw new InstanceNotFoundException("That user does not exist.");
        }
    }
}
