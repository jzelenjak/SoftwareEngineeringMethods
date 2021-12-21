package nl.tudelft.sem.courses.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.services.CourseService;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final String notAuthorized = "Not authorized";

    private final transient CourseService courseService;

    private final transient JwtUtils jwtUtils;

    /**
     * Constructs the CourseController class.
     *
     * @param courseService is the course service used to perform business logic.
     * @param jwtUtils is the JWT utility library used to decode JWT tokens.
     */
    public CourseController(CourseService courseService, JwtUtils jwtUtils) {
        this.courseService = courseService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Creates a new course. The request must provide a CourseRequest Object.
     * We assume that the front end creates this object and sends it to the user.
     *
     * @param request request object that must be supplied by the front end.
     * @return returns a http success or bad request
     */
    @PostMapping("/create")
    public boolean createNewCourse(@RequestBody CourseRequest request,
                                   @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken)) {
            String result = courseService.addNewCourses(request);
            if (result.contains("Failed")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to create new course");
            }
            return true;
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
        Jws<Claims> webtoken = isAuthorized(httpHeaders);

        if (checkIfStudent(webtoken) || checkIfLecturer(webtoken)) {
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
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken) || checkIfStudent(webtoken)) {
            Course course = courseService.getCourse(id);
            if (course == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find course with that ID");
            }
            CourseResponse courseResponse = new CourseResponse(
                    course.getId(),
                    course.getCourseCode(),
                    course.getStartDate(),
                    course.getFinishDate(),
                    course.getNumStudents());
            return courseResponse;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Removes a course in the courses repo if it exists.
     *
     * @param id -  Id of the course we want to delete
     * @return returns a http success or bad request
     */
    @PostMapping("/delete/{id}")
    public boolean deleteCourse(@PathVariable long id,
                                @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken)) {
            String result = courseService.deleteCourse(id);
            if (result.contains("Failed")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
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
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken)) {
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
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken) || checkIfStudent(webtoken)) {
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
    public List<Long> getCoursesOfLecturer(@PathVariable long lecturerId, @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken) || checkIfStudent(webtoken)) {
            List<Long> courseIds = courseService.getCourseIdForLecturer(lecturerId);
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
     * @param lecturerid - the id of the lecturer
     * @param courseid - the id of the course
     * @return if the operation was successful
     */
    @PostMapping("/create/lecturer/{lecturerid}/{courseid}")
    public Boolean addLecturerToCourse(@PathVariable("lecturerid") long lecturerid,
                                       @PathVariable("courseid") long courseid,
                                       @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfAdmin(webtoken)) {
            Boolean result = courseService.createTeaches(lecturerid, courseid);
            if (result) {
                return result;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Failed to assign Lecturer to Course");
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, notAuthorized);
    }

    /**
     * Checks if the lecturer is assigned to a course
     *
     * @param lecturerid - the id of the lecturer
     * @param courseid - the id of the course
     * @return true if lecturer teaches course otherwise false
     */
    @GetMapping("/get/teaches/{lecturerid}/{courseid}")
    public Boolean doesLecturerTeachesCourse(@PathVariable("lecturerid") long lecturerid,
                                             @PathVariable("courseid") long courseid,
                                             @RequestHeader HttpHeaders httpHeaders) {
        Jws<Claims> webtoken = isAuthorized(httpHeaders);
        if (checkIfLecturer(webtoken) || checkIfStudent(webtoken)) {
            Boolean result = courseService.lecturerTeachesCourse(lecturerid, courseid);
            if (result) {
                return result;
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
        String authHeader = httpHeaders.getFirst("Authorization");
        //if there is no such header return null
        if (authHeader == null) {
            return null;
        }
        //we create a new token
        String token = jwtUtils.resolveToken(authHeader);
        if (token == null) {
            return null;
        }
        //a Json webtoken containing the parsed JWS claims
        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(token);
        return claimsJws;
    }

    /**
     * Method checks if the role in webtoken is student or TA.
     *
     * @param claimsJws - a webtoken
     * @return - true if student/ta else false
     */
    public boolean checkIfStudent(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("STUDENT") || role.equals("TA");
    }

    /**
     * Method checks if role in webtoken is
     * lecturer or a admin.
     *
     * @param claimsJws - a webtoken
     * @return - true if lecturer/admin else false
     */
    public boolean checkIfLecturer(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("LECTURER") || role.equals("ADMIN");
    }

    /**
     * Method checks if role in webtoken is
     * admin.
     *
     * @param claimsJws - a webtoken
     * @return - true if admin else false
     */
    public boolean checkIfAdmin(Jws<Claims> claimsJws) {
        if (claimsJws == null) {
            return false;
        }
        String role = jwtUtils.getRole(claimsJws);
        return role.equals("ADMIN");
    }
}
