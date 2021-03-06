package nl.tudelft.sem.courses.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.EditionsResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.communication.MultiCourseRequest;
import nl.tudelft.sem.courses.communication.RecommendationRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.services.CourseService;
import nl.tudelft.sem.courses.util.CourseUtil;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/courses")
@Data
public class GradeController {

    private static final String notAuthorized = "Not authorized";
    private final transient CourseService courseService;
    private final transient JwtUtils jwtUtils;
    private final transient CourseUtil courseUtil;


    /**
     * Endpoint for getting multiple
     * user grades with specific restrictions.
     * You must provide a JSON or
     * Recommendation request object which contains the
     * following information:
     * course id
     * amount
     * minimum grade
     * user ids - for the users we want the grades for
     *
     *
     * @param recommendationRequest - recommendation request object
     * @return a map of user ids as keys and grade as values
     */
    @PostMapping("/statistics/user-grade")
    public Map<Long, Float> getMultipleUserGrades(
            @RequestBody RecommendationRequest recommendationRequest,
            @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken)) {
            Map<Long, Float> userGrades = courseService
                    .getMultipleUserGrades(recommendationRequest);
            if (userGrades == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to get user grades");
            }
            return userGrades;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Accepts a request to add a grade to the repository.
     * Passes on the grade to the courses service
     *
     * @param request -  a request object containing grade information
     * @return - a string confirming whether or not the method is successful.
     */
    @PostMapping("/create/grade")
    public boolean addGrade(@RequestBody GradeRequest request,
                            @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfAdmin(webToken) || (courseUtil.checkIfLecturer(webToken)
                && courseService.lecturerTeachesCourse(jwtUtils.getUserId(webToken),
                request.getCourseId()))) {
            if (request == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No Request was provided");
            }
            if (courseService.addGrade(request)) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Could not add grade to the repo");
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Returns the grade of a user for a specific course.
     *
     * @param userid   - the users id
     * @param courseId - the courses id
     * @return - a floating point value representing the grade.
     */
    @GetMapping("/get/grade/{userid}/{courseid}")
    public float getGradeOfUser(@PathVariable("userid") long userid,
                                @PathVariable("courseid") long courseId,
                                @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken) || courseUtil.checkIfStudent(webToken)) {
            Grade grade = courseService.getGrade(userid, courseId);
            if (grade == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find the grade for user and course");
            } else {
                return grade.getGradeValue();
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

}
