package nl.tudelft.sem.courses.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.CourseResponse;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.services.CourseService;
import nl.tudelft.sem.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



@RestController
@RequestMapping("/api/courses")
public class CourseController {


    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    transient CourseService courseService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Constructor for course controller.
     *
     * @param jwtUtils - A generable library to parse jwt tokens
     * @param courseService - courses service.
     */
    public CourseController(JwtUtils jwtUtils, CourseService courseService){
        this.jwtUtils = jwtUtils;
    }


    @GetMapping
    public @ResponseBody String getHelloWorld() {
        return "Hello world";
    }


    /**
     * Creates a new course. The request must provide a CourseRequest Object.
     * We assume that the front end creates this object and sends it to the user.
     *
     * @param request request object that must be supplied by the front end.
     * @return returns a http success or bad request
     */
    @PostMapping("/create/course")
    public String createNewCourse(@RequestBody CourseRequest request, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);
        if (authorized) {
            String result = courseService.addNewCourses(request);
            if (result.contains("Failed")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            return result;
        }
       throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    /**
     * Get a list of courses which made the course code
     *
     * @param code - the course code
     * @return - can return multiple courses with the same code
     */
    @PostMapping("/get/courses/{code}")
    public List<CourseResponse> getCoursesByCode(@PathVariable String code, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);

        if (authorized) {
            List<Course> courses = courseService.getCourses(code);
            if (courses != null &&  !courses.isEmpty()) {
                List<CourseResponse> courseResponses = new ArrayList<>();
                for(Course course: courses){
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    /**
     * Gives back course information including:
     * - Course id
     * - Course code
     * - Start date of the course
     * - End date of the course
     * - Number of students in the course
     *
     * @param id - id of the course
     * @return - A courseResponse object (simplified course object)
     */
    @PostMapping("/get/course/{id}")
    public CourseResponse getCourseById(@PathVariable long id, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);

        if (authorized) {

            Course course = courseService.getCourse(id);

            if (course == null) {
                throw new ResponseStatusException((HttpStatus.NOT_FOUND));
            }

            CourseResponse courseResponse = new CourseResponse(
                    course.getId(),
                    course.getCourseCode(),
                    course.getStartDate(),
                    course.getFinishDate(),
                    course.getGrades().size());

            return courseResponse;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    /**
     * Removes a course in the courses repo if it exists.
     *
     * @param id -  Id of the course we want to delete
     * @return returns a http success or bad request
     */
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable long id, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);
        if (authorized) {
            String result = courseService.deleteCourse(id);
            if (result.contains("Failed")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            return result;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }


    /**
     * Accepts a request to add a grade to the repository.
     * Passes on the grade to the courses service
     *
     * @param request -  a request object containing grade information
     * @return - a string confirming whether or not the method is successful.
     */
    @PostMapping("/create/grade")
    public String addGrade(@RequestBody GradeRequest request, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);

        if (authorized) {
            if (request == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            if (courseService.addGrade(request)) {
                return "Sucess! Grade has been added";
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    }


    /**
     *  Returns the grade of a user for a specific course.
     *
     * @param userid - the users id
     * @param courseId - the courses id
     * @return - a floating point value representing the grade.
     */
    @PostMapping("/get/grade/{userid}/{courseid}")
    public float getGradeOfUser(@PathVariable("userid") long userid, @PathVariable("courseid") long courseId, @RequestHeader HttpHeaders httpHeaders) throws Exception {

        Boolean authorized = isAuthorized(httpHeaders);

        if (authorized) {

            Grade grade = courseService.getGrade(userid, courseId);

            if (grade == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            } else {
                return grade.getGradeValue();
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    }





    /**
     * Method for taking in a jwt token and authorizing it for the user.
     *
     * @param httpHeaders - the header of a http request
     * @return - returns true if authenticated otherwise returns false
     *
     */
    public boolean isAuthorized(HttpHeaders httpHeaders)  {
        //first we try to get the authorization header information.
        String authHeader = httpHeaders.getFirst("Authorization");

        //if there is no such header return null
        if (authHeader == null) {
            return false;
        }
        //we create a new token
        String token = jwtUtils.resolveToken(authHeader);

        if (token == null) {
            return false;
        }

        //a Json webtoken containing the parsed JWS claims
        Jws<Claims> claimsJws = jwtUtils.validateAndParseClaims(token);

        if (claimsJws == null) {
            return false;
        }

        //now we check if there are any permissions for this method.
        String role = jwtUtils.getRole(claimsJws);

        if (role.equals("LECTURER")||role.equals("ADMIN")) {
            return true;

        }

        return false;



    }

}
