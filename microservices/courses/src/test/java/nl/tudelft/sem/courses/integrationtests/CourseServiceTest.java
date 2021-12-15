package nl.tudelft.sem.courses.integrationtests;


import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.services.CourseService;
import org.apache.tomcat.jni.Local;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CourseServiceTest {


    private CourseRepository courseRepository;
    private GradeRepository gradeRepository;
    private CourseService courseService;

    private final transient LocalDateTime dateAndTime = LocalDateTime.now();
    private final transient CourseRequest courseRequest = new CourseRequest("CSE2215", dateAndTime, dateAndTime);


    @BeforeEach
    void setup() {

        courseRepository = Mockito.mock(CourseRepository.class);
        gradeRepository = Mockito.mock(GradeRepository.class);
        courseService = new CourseService(courseRepository, gradeRepository);


    }



    @Test
    void addingNewCourseTest() {

        //returns nuthing
//        when(courseRepository.save(Mockito.any())).thenReturn(null);

        String result = courseService.addNewCourses(courseRequest);

        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(result, "Success. Added course");

    }


    @Test
    void addingNewDuplicateCourseTest() {

        String result = courseService.addNewCourses(courseRequest);

        List<Course> courseList = new ArrayList<>();

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        courseList.add(course);

        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list.
        String result2 = courseService.addNewCourses(courseRequest);

        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertEquals(result, "Success. Added course");
        Assert.assertEquals(result2, "Failed");

    }
    @Test
    void addingTwoCoursesWithSameCourseCode() {

        CourseRequest courseRequest2 = new CourseRequest("CSE2215",
                dateAndTime, LocalDateTime.MAX);


        String result = courseService.addNewCourses(courseRequest);

        List<Course> courseList = new ArrayList<>();

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        courseList.add(course);
//        verify(courseRepository, Mockito.times(1)).save(Mockito.any());

        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list.
        String result2 = courseService.addNewCourses(courseRequest2);

        verify(courseRepository, Mockito.times(2)).save(Mockito.any());
        Assert.assertEquals( "Success. Added course", result);
        Assert.assertEquals("Success. Added course", result2);

    }


    //Now tests for the second service method deleting a course. First we have to add a course.

    @Test
    void deletingAnExistingCourse() {



        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);



        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(1)).delete(Mockito.any());
        Assert.assertEquals("Success. Deleted course", result);
    }


    @Test
    void deletingANonExistingCourse() {


        Optional<Course> optionalCourse = Optional.ofNullable(null);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals("Failed", result);
    }

    @Test
    void deleteOfCourseFails() {


//        Optional<Course> optionalCourse = Optional.ofNullable(null);
//        when(courseRepository.findById(Mockito.any())).thenThrow(IllegalArgumentException.class);

        doThrow(IllegalArgumentException.class).when(courseRepository).delete(Mockito.any());

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals("Failed", result);
    }

    //testing getting the list of all courses with matching course code.
    @Test
    void gettingCourses() {

        List<Course> courseList = new ArrayList<>();

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        courseList.add(course);
//        verify(courseRepository, Mockito.times(1)).save(Mockito.any());

        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list.

        List<Course> courses = courseService.getCourses("CSE2215");
        verify(courseRepository, Mockito.times(1)).findAllByCourseCode(Mockito.any());
        Assert.assertEquals(courseList, courses);


    }


    @Test
    void gettingExistingGrade() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);


        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(1)).findByUserIdAndCourse(1, course);
        Assert.assertEquals(grade, result);


    }

    @Test
    void OptionalGradeIsNull() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);


        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.ofNullable(null);

        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(1)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);


    }

    @Test
    void OptionalCourseIsEmpty() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.ofNullable(null);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);


        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(0)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);


    }

    @Test
    void repositoryMethodThrowsException() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.of(course);

        doThrow(IllegalArgumentException.class).when(courseRepository).findById(Mockito.any());


        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Grade> optionalGrade = Optional.of(grade);

        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        Grade result = courseService.getGrade(1, 1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(gradeRepository, Mockito.times(0)).findByUserIdAndCourse(1, course);
        Assert.assertNull(result);


    }

    //we do it now for the getCoures method.

    @Test
    void gettingAValidCourse() {


        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

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
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Optional<Course> optionalCourse = Optional.ofNullable(null);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        Course result = courseService.getCourse(1);

        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        Assert.assertNull(result);

    }


    @Test
    void addingAValidGradeForUser() {
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);


        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.ofNullable(null);



        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertTrue(result);
    }

    @Test
    void addingAExistingGradeForUser() {
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);


        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.of(grade);



        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);

        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(0)).save(Mockito.any());
        Assert.assertFalse(result);
    }

    @Test
    void addingGradeThrewException() {
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);


        Course course = new Course();
        course.setId(1);
        course.setStartDate(dateAndTime);
        course.setFinishDate(dateAndTime);
        course.setCourseCode("CSE2215");

        Grade grade = new Grade();
        grade.setUserId(1);
        grade.setGradeValue(2.0f);
        grade.setId(2);
        grade.setCourse(course);

        Optional<Course> optionalCourse = Optional.of(course);

        Optional<Grade> optionalGrade = Optional.of(grade);



        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(gradeRepository.findByUserIdAndCourse(1,course)).thenReturn(optionalGrade);
        doThrow(IllegalArgumentException.class).when(gradeRepository).save(grade);

        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(0)).save(Mockito.any());
        Assert.assertFalse(result);
    }
}
