package nl.tudelft.sem.courses.services;

import java.util.List;
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
        List<Course> courses = courseRepository.findAllByCourseCode(request.getCourseCode());


        if (courses.isEmpty()) {
            Course newCourse = new Course();
            newCourse.setCourseCode(request.getCourseCode());
            newCourse.setStartDate(request.getStartDate());
            newCourse.setFinishDate(request.getFinishDate());
            courseRepository.save(newCourse);

            return "Success. Added course";
        } else {
            Course newCourse = new Course();
            newCourse.setCourseCode(request.getCourseCode());
            newCourse.setStartDate(request.getStartDate());
            newCourse.setFinishDate(request.getFinishDate());

            if (courses.contains(newCourse)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            } else {
                courseRepository.save(newCourse);
                return "Success. Added course";
            }
        }
    }

    /**
     * This method attempts to remove an existing course.
     *
     * @param courseID - id of the course we want to delete
     * @return - returns whether delete was successful
     * @throws Exception - http exceptions for users
     */
    public String deleteCourse(long courseID) throws Exception {
        Optional<Course> course = courseRepository.findByCourseId(courseID);

        if (!course.isEmpty()) {
            try{
                courseRepository.delete(course.get());
                return "Success. Added course";
            } catch (Exception e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    public List<Course> getCourses(String courseCode) {
        return courseRepository.findAllByCourseCode(courseCode);
    }

}
