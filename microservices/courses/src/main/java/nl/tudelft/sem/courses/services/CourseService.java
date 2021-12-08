package nl.tudelft.sem.courses.services;

import java.util.Optional;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourseService {


    @Autowired
    private transient CourseRepository courseRepository;

    /**
     * This method adds new courses to the repo.
     * The request must be through the controller.
     *
     * @param request - The request
     * @return String - returns successful string.
     * @throws Exception - This exception is a http exception
     */
    public String addNewCourses(CourseRequest request) throws Exception {
        Optional<Course> course = courseRepository.findByCourseId(request.getCourseId());


        if (course.isEmpty()) {
            Course newCourse = new Course();
            newCourse.setCourseId(request.getCourseId());
            newCourse.setStartDate(request.getStartDate());
            newCourse.setFinishDate(request.getFinishDate());
            courseRepository.save(newCourse);

            return "Success. Added course";
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
}
