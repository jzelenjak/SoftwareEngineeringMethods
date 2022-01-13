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
public class CourseController {

    private static final String notAuthorized = "Not authorized";
    private final transient CourseService courseService;
    private final transient JwtUtils jwtUtils;
    private final transient ObjectMapper objectMapper;
    private final transient CourseUtil courseUtil;


    /**
     * Creates a new course. The request must provide a CourseRequest Object.
     * We assume that the front end creates this object and sends it to the user.
     *
     * @param request request object that must be supplied by the front end.
     * @return returns a http success or bad request
     */
    @PostMapping("/create")
    public CourseResponse createNewCourse(@RequestBody CourseRequest request,
                                  @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfAdmin(webToken)) {
            CourseResponse result = courseService.addNewCourses(request);
            if (result == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to create new course");
            }
            return result;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Get a list of courses which made the course code.
     *
     * @param code - the course code
     * @return - can return multiple courses with the same code
     */
    @GetMapping("/get/courses/{code}")
    public List<CourseResponse> getCoursesByCode(
            @PathVariable String code, @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);

        if (courseUtil.checkIfStudent(webToken) || courseUtil.checkIfLecturerOrAdmin(webToken)) {
            List<Course> courses = courseService.getCourses(code);
            if (courses != null && !courses.isEmpty()) {
                List<CourseResponse> courseResponses = new ArrayList<>();
                for (Course course : courses) {
                    CourseResponse courseResponse = new CourseResponse(
                            course.getId(),
                            course.getCourseCode(),
                            course.getStartDate(),
                            course.getFinishDate(),
                            course.getGrades().size());
                    courseResponses.add(courseResponse);
                }
                return courseResponses;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find courses for that code");
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Gives back course information including.
     * - Course id
     * - Course code
     * - Start date of the course
     * - End date of the course
     * - Number of students in the course
     *
     * @param id - id of the course
     * @return - A courseResponse object (simplified course object)
     */
    @GetMapping("/get/{id}")
    public CourseResponse getCourseById(@PathVariable long id,
                                        @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken) || courseUtil.checkIfStudent(webToken)) {
            Course course = courseService.getCourse(id);
            if (course == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find course with that ID");
            }
            return new CourseResponse(
                    course.getId(),
                    course.getCourseCode(),
                    course.getStartDate(),
                    course.getFinishDate(),
                    course.getNumStudents());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Gives back a list that contains information of multiple requested courses.
     *
     * @param body - JSON object that contains a list of course IDs
     * @return - A map of CourseResponse objects (simplified course objects) in JSON format
     */
    @PostMapping("/get-multiple")
    public String getMultipleCourses(@RequestBody MultiCourseRequest body,
                                     @RequestHeader HttpHeaders httpHeaders) {
        // Check if the user is authorized
        if (courseUtil.isAuthorized(httpHeaders) != null) {
            // Retrieve a list of courses associated to the IDs
            List<Course> courses = courseService.getMultipleCourses(body.getCourseIds());

            // Compose JSON object with course information
            ObjectNode json = objectMapper.createObjectNode();
            courses.forEach(course -> json.set(String.valueOf(course.getId()),
                    objectMapper.valueToTree(new CourseResponse(course))));

            // Return the JSON object as string
            return json.toString();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Endpoint takes course id as input.
     * It gives back list of course ids for
     * courses which have the same course code
     * as the course in the input.
     *
     * @param courseId - The id of input course.
     * @return - List of courses with matching course code.
     */
    @GetMapping("/get-all-editions")
    public EditionsResponse getAllEditionsOfCourse(@RequestParam Long courseId,
                                                   @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken)) {
            List<Long> courseIds = courseService.getAllEditionsOfCourse(courseId);
            if (courseIds == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to get course editions");
            }
            return new EditionsResponse(courseIds);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Removes a course in the courses repo if it exists.
     *
     * @param id -  Id of the course we want to delete
     * @return returns a http success or bad request
     */
    @DeleteMapping("/delete/{id}")
    public boolean deleteCourse(@PathVariable long id,
                                @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfAdmin(webToken)) {
            String result = courseService.deleteCourse(id);
            if (result.contains("Failed")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to delete course");
            }
            return true;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }




    /**
     * Returns all the course ids for a lecturer.
     *
     * @param lecturerId - id of the lecturer
     * @return list of course ids
     */
    @GetMapping("/get/lecturer/courses/{lecturerId}")
    public List<Long> getCoursesOfLecturer(@PathVariable long lecturerId,
                                           @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken) || courseUtil.checkIfStudent(webToken)) {
            List<Long> courseIds = courseService.getCourseIdsForLecturer(lecturerId);
            if (courseIds == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find any courses for lecturer");
            } else {
                return courseIds;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Assigns a lecturer to a course.
     *
     * @param lecturerId - the id of the lecturer
     * @param courseId   - the id of the course
     * @return if the operation was successful
     */
    @PostMapping("/assign/lecturer/{lecturerId}/{courseId}")
    public Boolean addLecturerToCourse(@PathVariable("lecturerId") long lecturerId,
                                       @PathVariable("courseId") long courseId,
                                       @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfAdmin(webToken)) {
            Boolean result = courseService.createTeaches(lecturerId, courseId);
            if (result) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to assign Lecturer to Course");
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Checks if the lecturer is assigned to a course.
     *
     * @param lecturerId - the id of the lecturer
     * @param courseId   - the id of the course
     * @return true if lecturer teaches course otherwise false
     */
    @GetMapping("/get/teaches/{lecturerId}/{courseId}")
    public Boolean doesLecturerTeachCourse(@PathVariable("lecturerId") long lecturerId,
                                           @PathVariable("courseId") long courseId,
                                           @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = courseUtil.isAuthorized(httpHeaders);
        if (courseUtil.checkIfLecturerOrAdmin(webToken) || courseUtil.checkIfStudent(webToken)) {
            Boolean result = courseService.lecturerTeachesCourse(lecturerId, courseId);
            if (result) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lecturer does not teach course");
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }
}
