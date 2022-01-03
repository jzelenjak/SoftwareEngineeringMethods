package nl.tudelft.sem.courses.integrationtests;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.services.CourseService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CourseServiceTest {

    private transient CourseRepository courseRepository;
    private transient GradeRepository gradeRepository;
    private transient CourseService courseService;

    private final transient ZonedDateTime date = ZonedDateTime.now();
    private final transient CourseRequest courseRequest =
            new CourseRequest("CSE2215", date, date, 1);
    private static final String courseCode = "CSE2215";
    private static final String addedCourse = "Success. Added course";

    @BeforeEach
    void setup() {
        courseRepository = Mockito.mock(CourseRepository.class);
        gradeRepository = Mockito.mock(GradeRepository.class);
        courseService = new CourseService(courseRepository, gradeRepository);
    }

    @Test
    void addingNewCourseTest() {
        String result = courseService.addNewCourses(courseRequest);
        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(result, addedCourse);

    }

    @Test
    void addingNewDuplicateCourseTest() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        course.setNumStudents(1);

        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        String result = courseService.addNewCourses(courseRequest);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);
        String result2 = courseService.addNewCourses(courseRequest);
        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(result, addedCourse);
        Assert.assertEquals(result2, "Failed");

    }

    @Test
    void addingTwoCoursesWithSameCourseCode() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        course.setNumStudents(1);
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        String result = courseService.addNewCourses(courseRequest);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list.
        CourseRequest courseRequest2 = new CourseRequest(courseCode,
                date, ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), 1);
        String result2 = courseService.addNewCourses(courseRequest2);

        verify(courseRepository, Mockito.times(2)).save(Mockito.any());
        Assert.assertEquals(addedCourse, result);
        Assert.assertEquals(addedCourse, result2);

    }
    //Now tests for the second service method deleting a course. First we have to add a course.

    @Test
    void deletingAnExistingCourse() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(1)).delete(Mockito.any());
        Assert.assertEquals("Success. Deleted course", result);
    }


    @Test
    void deletingAnonExistingCourse() {

        Optional<Course> optionalCourse = Optional.ofNullable(null);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals("Failed", result);
    }

    @Test
    void deleteOfCourseFails() {
        doThrow(IllegalArgumentException.class).when(courseRepository).delete(Mockito.any());

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals("Failed", result);
    }

    //testing getting the list of all courses with matching course code.
    @Test
    void gettingCourses() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list
        List<Course> courses = courseService.getCourses(courseCode);
        verify(courseRepository, Mockito.times(1)).findAllByCourseCode(Mockito.any());
        Assert.assertEquals(courseList, courses);
    }


    @Test
    void gettingExistingGrade() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        Optional<Course> optionalCourse = Optional.of(course);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(1)).findByUserIdAndCourse(1, course);
        Assert.assertEquals(grade, result);
    }

    @Test
    void optionalGradeIsNull() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.of(course);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);
        Optional<Grade> optionalGrade = Optional.ofNullable(null);

        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(1)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);
    }

    @Test
    void optionalCourseIsEmpty() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.ofNullable(null);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(0)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);
    }

    @Test
    void repositoryMethodThrowsException() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        doThrow(IllegalArgumentException.class).when(courseRepository).findById(Mockito.any());

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(0)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);
    }

    //we do it now for the getCoures method.

    @Test
    void gettingAvalidCourse() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        Course result = courseService.getCourse(1);

        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        Assert.assertEquals(course, result);
    }

    @Test
    void gettingAnInvalidCourse() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.ofNullable(null);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        Course result = courseService.getCourse(1);

        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        Assert.assertNull(result);
    }

    @Test
    void addingAvalidGradeForUser() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.ofNullable(null);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);
        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertTrue(result);
    }

    @Test
    void addingAexistingGradeForUser() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);
        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(0)).save(Mockito.any());
        Assert.assertFalse(result);
    }

    @Test
    void addingGradeThrewException() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1, course)).thenReturn(optionalGrade);
        doThrow(IllegalArgumentException.class).when(gradeRepository).save(grade);
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);
        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(0)).save(Mockito.any());
        Assert.assertFalse(result);
    }
}