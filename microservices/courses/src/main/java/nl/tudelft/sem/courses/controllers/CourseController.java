package nl.tudelft.sem.courses.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.communication.MultiCourseRequest;
import nl.tudelft.sem.courses.communication.EditionsResponse;
import nl.tudelft.sem.courses.communication.RecommendationRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.services.CourseService;
import nl.tudelft.sem.jwt.JwtUtils;
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
public class CourseController {

    private static final String notAuthorized = "Not authorized";

    private final transient CourseService courseService;

    private final transient JwtUtils jwtUtils;

    private final transient ObjectMapper objectMapper;

    /**
     * Constructs the CourseController class.
     *
     * @param courseService is the course service used to perform business logic.
     * @param jwtUtils      is the JWT utility library used to decode JWT tokens.
     * @param objectMapper  is the object mapper used to convert objects to JSON.
     */
    public CourseController(CourseService courseService, JwtUtils jwtUtils,
                            ObjectMapper objectMapper) {
        this.courseService = courseService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new course. The request must provide a CourseRequest Object.
     * We assume that the front end creates this object and sends it to the user.
     *
     * @param request request object that must be supplied by the front end.
     * @return returns a http success or bad request
     */
    @PostMapping("/create")
    public Course createNewCourse(@RequestBody CourseRequest request,
                                  @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfAdmin(webToken)) {
            Course result = courseService.addNewCourses(request);
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);

        if (checkIfStudent(webToken) || checkIfLecturerOrAdmin(webToken)) {
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfLecturerOrAdmin(webToken) || checkIfStudent(webToken)) {
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
        if (isAuthorized(httpHeaders) != null) {
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if(checkIfLecturerOrAdmin(webToken)) {
            List<Long> courseIds = courseService.getAllEditionsOfCourse(courseId);
            if (courseIds == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to get course editions");
            }
            return new EditionsResponse(courseIds);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    @GetMapping("/statistics/user-grade")
    public Map<Long, Float> getMultipleUserGrades(@RequestBody RecommendationRequest recommendationRequest,
                                                   @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if(checkIfLecturerOrAdmin(webToken)) {
            Map<Long, Float> userGrades = courseService.getMultipleUserGrades(recommendationRequest);
            if (userGrades == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to get user grades");
            }
            return userGrades;
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfAdmin(webToken)) {
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
     * Accepts a request to add a grade to the repository.
     * Passes on the grade to the courses service
     *
     * @param request -  a request object containing grade information
     * @return - a string confirming whether or not the method is successful.
     */
    @PostMapping("/create/grade")
    public boolean addGrade(@RequestBody GradeRequest request,
                            @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfAdmin(webToken)||checkIfLecturer(webToken)&&courseService.lecturerTeachesCourse(jwtUtils.getUserId(webToken), request.getCourseId())) {
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfLecturerOrAdmin(webToken) || checkIfStudent(webToken)) {
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

    /**
     * Returns all the course ids for a lecturer.
     *
     * @param lecturerId - id of the lecturer
     * @return list of course ids
     */
    @GetMapping("/get/lecturer/courses/{lecturerId}")
    public List<Long> getCoursesOfLecturer(@PathVariable long lecturerId,
                                           @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfLecturerOrAdmin(webToken) || checkIfStudent(webToken)) {
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfAdmin(webToken)) {
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
        Jws<Claims> webToken = isAuthorized(httpHeaders);
        if (checkIfLecturerOrAdmin(webToken) || checkIfStudent(webToken)) {
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

    /**
     * Method for taking in a jwt token and authorizing it for the user.
     *
     * @param httpHeaders - the header of a http request
     * @return - returns true if authenticated otherwise returns false
     */
    public Jws<Claims> isAuthorized(HttpHeaders httpHeaders) {
        //first we try to get the authorization header information.
        String authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        //if there is no such header return null
        if (authHeader == null) {
            return null;
        }
        //we create a new token
        String token = jwtUtils.resolveToken(authHeader);
        if (token == null) {
            return null;
        }
        //a Json webToken containing the parsed JWS claims
        return jwtUtils.validateAndParseClaims(token);
    }

    /**
     * Method checks if the role in webToken is student.
     *
     * @param claimsJws - a webToken
     * @return - true if student/ta else false
     */
    public boolean checkIfStudent(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("STUDENT");
    }

    /**
     * Method checks if role in webToken is a lecturer or an admin.
     *
     * @param claimsJws - a webToken
     * @return - true if lecturer/admin, false otherwise
     */
    public boolean checkIfLecturerOrAdmin(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("LECTURER") || role.equals("ADMIN");
    }

    /**
     * Method checks if role in web token is
     * admin.
     *
     * @param claimsJws - a web token
     * @return - true if admin else false
     */
    public boolean checkIfAdmin(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("ADMIN");
    }
    /**
     * Method checks if role in web token is
     * lecturer.
     *
     * @param claimsJws - a web token
     * @return - true if lecturer else false
     */
    public boolean checkIfLecturer(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("ADMIN");
    }
}
