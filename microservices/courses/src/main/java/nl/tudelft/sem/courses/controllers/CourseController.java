package nl.tudelft.sem.courses.controllers;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PostMapping("/create")
    public String createNewCourse(@RequestBody CourseRequest request) throws Exception {
        //TODO
        //Add authorization
        return courseService.addNewCourses(request);

    }

    /**
     * Removes a course in the courses repo if it exists.
     *
     * @param courseID -  Id of the course we want to delete
     * @return returns a http success or bad request
     */
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable long courseID) throws Exception {
        //TODO
        //Add authorization
        return courseService.deleteCourse(courseID);
    }

    /**
     * Get a list of courses which made the course code
     *
     * @param code - the course code
     * @return - can return multiple courses with the same code
     */
    @PostMapping("/get/courses/{code}")
    public List<Course> getCourses(@PathVariable String code) {
        //TODO
        //Add authorization
        List<Course> courses = courseService.getCourses(code);
        if (!courses.isEmpty()) {
            return courses;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /**
     *  Returns the grade of a user for a specific course.
     *
     * @param userid - the users id
     * @param courseId - the courses id
     * @return - a floating point value representing the grade.
     */
    @PostMapping("/get/grade/{userid}/{courseid}")
    public float getGradeOfUser(@PathVariable("userid") long userid, @PathVariable("courseid") long courseId) {
        //TODO
        //Add authorization
        Grade grade = courseService.getGrade(userid, courseId);

        if (grade == null) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return grade.getGradeValue();
        }

    }

       


}
