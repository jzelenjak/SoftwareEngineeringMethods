package nl.tudelft.sem.courses.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourseService {



    private transient CourseRepository courseRepository;

    private transient GradeRepository gradeRepository;

    /**
     * Constructor for Dependency Injection.
     *
     * @param courseRepository - A repository object for courses
     * @param gradeRepository -  A repository object for grades
     */
    public CourseService(CourseRepository courseRepository, GradeRepository gradeRepository) {
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
    }

    /**
     * This method adds new courses to the repo.
     * The request must be through the controller.
     *
     * @param request - The request
     * @return String - returns successful string.
     */
    public String addNewCourses(CourseRequest request)  {
        List<Course> courses = courseRepository.findAllByCourseCode(request.getCourseCode());


        if (courses.isEmpty()) {
            Course newCourse = new Course(request.getCourseCode(), request.getStartDate(),
                    request.getFinishDate(), request.getNumStudents());
            courseRepository.save(newCourse);

            return "Success. Added course";
        } else {
            Course newCourse = new Course(request.getCourseCode(), request.getStartDate(),
                    request.getFinishDate(), request.getNumStudents());
            if (courses.contains(newCourse)) {
                return "Failed";
            } else {
                courseRepository.save(newCourse);
                return "Success. Added course";
            }
        }
    }

    /**
     * This method attempts to remove an existing course.
     *
     * @param courseId - id of the course we want to delete
     * @return - returns whether delete was successful
     */
    public String deleteCourse(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isEmpty()) {
            try {
                courseRepository.delete(course.get());
                return "Success. Deleted course";
            } catch (Exception e) {
                return "Failed";
            }
        }
        return "Failed";
    }

    /**
     * Returns a list of all course with the matching course code.
     *
     * @param courseCode - Course code of the courses
     * @return - List of all courses with matching course code
     */
    public List<Course> getCourses(String courseCode) {
        return courseRepository.findAllByCourseCode(courseCode);
    }

    /**
     * Returns a list of courses associated to the given IDs.
     *
     * @param courseIds - Set of course IDs
     * @return - List of courses associated to the given IDs
     */
    public List<Course> getMultipleCourses(Set<Long> courseIds) {
        return courseRepository.findAllByIds(courseIds);
    }

    /**
     * Gives back the grade of a user for a specific course.
     *
     * @param userId - the id of the user.
     * @param courseId - the id of the course.
     * @return - if grade is found returns the grade
     *           otherwise it returns null.
     */
    public Grade getGrade(long userId, long courseId) {
        //First we get the course and see if it exists.
        try {
            Optional<Course> course = courseRepository.findById(courseId);

            if (course.isEmpty()) {
                return null;
            } else {
                //We try to get the grade of the user.
                Optional<Grade> grade = gradeRepository.findByUserIdAndCourse(userId, course.get());

                if (grade.isEmpty()) {
                    return null;
                } else {
                    return grade.get();
                }
            }
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Gives the course corresponding to a specific course id.
     *
     * @param courseId - the id of the course (not the course code)
     * @return - A course object
     */
    public Course getCourse(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isEmpty()) {
            return course.get();
        } else {
            return null;
        }

    }

    /**
     * Adds a grade to the repository.
     *
     * @param request -  a grade request object containing all
     *                the necessary objects for repository
     * @return - if succeeded returns true otherwise returns false.
     */
    public boolean addGrade(GradeRequest request) {
        //checking if a grade already exists for this user
        Grade existingGrade = getGrade(request.getUserId(), request.getCourseId());

        if (existingGrade != null && existingGrade.getGradeValue() >= request.getGrade()) {
            return false;
        }

        try {
            if (existingGrade == null) {
                Course course = getCourse(request.getCourseId());
                Grade grade = new Grade(course, request.getUserId(), request.getGrade());
                gradeRepository.save(grade);
            } else {
                existingGrade.setGradeValue(request.getGrade());
                gradeRepository.save(existingGrade);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
