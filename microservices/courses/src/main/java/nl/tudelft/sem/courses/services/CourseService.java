package nl.tudelft.sem.courses.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.entities.Teaches;
import nl.tudelft.sem.courses.entities.TeachesPk;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.respositories.TeachesRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseService {



    private transient CourseRepository courseRepository;

    private transient GradeRepository gradeRepository;

    private transient TeachesRepository teachesRepository;

    /**
     * Constructor for Dependency Injection.
     *
     * @param courseRepository - A repository object for courses
     * @param gradeRepository -  A repository object for grades
     */
    public CourseService(CourseRepository courseRepository,
                         GradeRepository gradeRepository,
                         TeachesRepository teachesRepository) {
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
        this.teachesRepository = teachesRepository;
    }

    /**
     * This method adds new courses to the repo.
     * The request must be through the controller.
     *
     * @param request - The request
     * @return long - returns positive number upon successful completion,
     *      otherwise returns -1
     */
    public Course addNewCourses(CourseRequest request)  {
        List<Course> courses = courseRepository.findAllByCourseCode(request.getCourseCode());

        try {
            if (courses.isEmpty()) {
                Course newCourse = new Course(request.getCourseCode(), request.getStartDate(),
                        request.getFinishDate(), request.getNumStudents());
                courseRepository.save(newCourse);
                courseRepository.flush();
                return newCourse;
            } else {
                Course newCourse = new Course(request.getCourseCode(), request.getStartDate(),
                        request.getFinishDate(), request.getNumStudents());
                if (courses.contains(newCourse)) {
                    return null;
                } else {
                    courseRepository.save(newCourse);
                    courseRepository.flush();
                    return newCourse;
                }
            }
        } catch (Exception e) {
            return null;
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
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gives all the course ids for the
     * courses the lecturer teaches.
     *
     * @param lecturerId - id of the lecturer
     * @return list of course id's
     */
    public List<Long> getCourseIdsForLecturer(long lecturerId) {
        List<Teaches> list = teachesRepository.findAllByLecturerId(lecturerId);

        if (list == null || list.isEmpty()) {
            return null;
        }
        List<Long> result = list.stream().map(teaches -> teaches.getCourseId())
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Checks if the lecturer teaches a specific course.
     *
     * @param lecturerId - lecturer's id
     * @param courseId - the courses id
     * @return true if lecturer teaches course otherwise false
     */
    public Boolean lecturerTeachesCourse(long lecturerId, long courseId) {
        Optional<Teaches> teachesCourse = teachesRepository
                .findById(new TeachesPk(courseId, lecturerId));
        if (teachesCourse.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Assigns a lecturer to a course.
     * This is done by creating a teach entity
     *
     * @param lecturerId - lecturer's id
     * @param courseId - id of the course
     * @return if operation was successful or not
     */
    public Boolean createTeaches(long lecturerId, long courseId) {
        try {
            Teaches teach = new Teaches(courseId, lecturerId);
            teachesRepository.save(teach);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
