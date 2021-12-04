package nl.tudelft.sem.courses.controllers;

import java.util.Optional;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
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
    private transient CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;



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
    public String createNewCourse(@RequestBody CourseRequest request) {
        Optional<Course> course = courseRepository.findByCourseId(request.courseId);

        if (course.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Course newCourse = new Course();
        newCourse.setCourseId(request.courseId);
        newCourse.setStartDate(request.startDate);
        courseRepository.save(newCourse);

        return "Success. Added course";

    }

    /**
     * This method edits the course if it finds it in the directory.
     *
     * @param request Course request object
     * @return returns a http success or bad request
     */
    @PostMapping("/edit")
    public String editCourse(@PathVariable CourseRequest request) {
        Optional<Course> course = courseRepository.findByCourseId(request.courseId);
        if (course.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return "Success. Edited Course";
    }

}
