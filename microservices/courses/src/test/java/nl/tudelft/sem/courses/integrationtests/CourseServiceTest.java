package nl.tudelft.sem.courses.integrationtests;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.communication.CourseRequest;
import nl.tudelft.sem.courses.communication.GradeRequest;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.entities.Teaches;
import nl.tudelft.sem.courses.entities.TeachesPk;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.respositories.TeachesRepository;
import nl.tudelft.sem.courses.services.CourseService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.tags.form.InputTag;
import org.xmlunit.builder.Input;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CourseServiceTest {

    private transient CourseRepository courseRepository;
    private transient GradeRepository gradeRepository;
    private transient CourseService courseService;
    private transient TeachesRepository teachesRepository;

    private final transient ZonedDateTime date = ZonedDateTime.now();
    private final transient CourseRequest courseRequest =
            new CourseRequest("CSE2215", date, date, 1);
    private static final String courseCode = "CSE2215";
    private static final String addedCourse = "Success. Added course";
    private static final String failedString = "Failed";
    private static final Teaches teaches = new Teaches(1, 1);


    @BeforeEach
    void setup() {
        courseRepository = Mockito.mock(CourseRepository.class);
        gradeRepository = Mockito.mock(GradeRepository.class);
        teachesRepository = Mockito.mock(TeachesRepository.class);
        courseService = new CourseService(courseRepository, gradeRepository, teachesRepository);
    }

    @Test
    void testAddNewCourse() {
        Course result = courseService.addNewCourses(courseRequest);
        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getId() >= 0);

    }

    @Test
    void testAddNewCourseDuplicate() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        course.setNumStudents(1);

        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        Course result = courseService.addNewCourses(courseRequest);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getId() >= 0);
        Course result2 = courseService.addNewCourses(courseRequest);
        Assert.assertNull(result2);

    }

    @Test
    void testAddNewCourseAddingTwoCoursesWithSameCourseCode() {

        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);
        course.setNumStudents(1);
        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        Course result = courseService.addNewCourses(courseRequest);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courseList);

        //second iteration of adding the course list.

        verify(courseRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getId() >= 0);
        CourseRequest courseRequest2 = new CourseRequest(courseCode,
                date, ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), 1);
        Course result2 = courseService.addNewCourses(courseRequest2);
        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.getId() >= 0);

    }
    //Now tests for the second service method deleting a course. First we have to add a course.

    @Test
    void testDeleteCourseWithExistingCourse() {
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
    void testDeleteCourseWithNonExistingCourse() {

        Optional<Course> optionalCourse = Optional.ofNullable(null);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals(failedString, result);
    }

    @Test
    void testDeleteCourseFails() {
        doThrow(IllegalArgumentException.class).when(courseRepository).delete(Mockito.any());

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(0)).delete(Mockito.any());
        Assert.assertEquals(failedString, result);
    }


    @Test
    void testDeleteCourseAnExistingCourseException() {
        Course course = new Course();
        course.setId(1);
        course.setStartDate(date);
        course.setFinishDate(date);
        course.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.of(course);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        doThrow(IllegalArgumentException.class).when(courseRepository).delete(Mockito.any());

        String result = courseService.deleteCourse(1);
        verify(courseRepository, Mockito.times(1)).delete(Mockito.any());
        Assert.assertEquals(failedString, result);
    }


    //testing getting the list of all courses with matching course code.
    @Test
    void testGetCourses() {

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
    void testGetGradeExistingGrade() {

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
    void testGetGradeOptionalGradeIsNull() {

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
    void testGetGradeOptionalCourseIsEmpty() {

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
    void testGetGradeRepositoryMethodThrowsException() {
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
    void testGetCourseWithValidCourse() {
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
    void testGetCourseWithInvalidCourse() {
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
    void testGetAllEditionsOfCourseValidList() {
        Course inputCourse = new Course();
        inputCourse.setId(1);
        inputCourse.setStartDate(date);
        inputCourse.setFinishDate(date);
        inputCourse.setCourseCode(courseCode);

        List<Course> courses = Arrays.asList(inputCourse);

        Optional<Course> optionalCourse = Optional.ofNullable(inputCourse);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(courses);

        List<Long> result = courseService.getAllEditionsOfCourse(1);
        List<Long> expectedResult = Arrays.asList(1L);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(courseRepository, Mockito.times(1)).findAllByCourseCode(Mockito.any());

        Assert.assertEquals(result, expectedResult);
    }

    @Test
    void testGetAllEditionsOfCourseNoCourseFoundWithGivenId() {
        List<Long> result = courseService.getAllEditionsOfCourse(1);
        Assert.assertNull(result);
    }

    @Test
    void testGetAllEditionsOfCourseFailedToFindAllCoursesByCourseCode() {
        Course inputCourse = new Course();
        inputCourse.setId(1);
        inputCourse.setStartDate(date);
        inputCourse.setFinishDate(date);
        inputCourse.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.ofNullable(inputCourse);

        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        when(courseRepository.findAllByCourseCode(Mockito.any())).thenReturn(null);

        List<Long> result = courseService.getAllEditionsOfCourse(1);
        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(courseRepository, Mockito.times(1)).findAllByCourseCode(Mockito.any());

        Assert.assertNull(result);
    }

    @Test
    void testGetAllEditionsOfCourseExceptionThrown() {
        Course inputCourse = new Course();
        inputCourse.setId(1);
        inputCourse.setStartDate(date);
        inputCourse.setFinishDate(date);
        inputCourse.setCourseCode(courseCode);

        Optional<Course> optionalCourse = Optional.ofNullable(inputCourse);
        when(courseRepository.findById(Mockito.any())).thenReturn(optionalCourse);
        doThrow(IllegalArgumentException.class)
                .when(courseRepository).findAllByCourseCode(Mockito.any());
        List<Long> result = courseService.getAllEditionsOfCourse(1);

        verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        verify(courseRepository, Mockito.times(1)).findAllByCourseCode(Mockito.any());

        Assert.assertNull(result);
    }

    @Test
    void testAddGradeWithValidGradeForUser() {
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
    void testAddGradeWithValidGradeForUserException() {
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
        doThrow(IllegalArgumentException.class).when(gradeRepository).save(Mockito.any());
        GradeRequest gradeRequest = new GradeRequest(1, 2.0f, 1);
        boolean result = courseService.addGrade(gradeRequest);
        verify(gradeRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertFalse(result);
    }

    @Test
    void testAddGradeWithExistingGradeForUser() {
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
    void testAddGradeButFailed() {
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

        Grade grade2 = new Grade();
        grade2.setUserId(1);
        grade2.setGradeValue(1.0f);
        grade2.setId(2);
        grade2.setCourse(course);

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
    void testAddGradeThrewException() {
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

    @Test
    void testGetCourseIdsForLecturer() {
        Teaches teaches1 = new Teaches(2, 1);
        Teaches teaches2 = new Teaches(3, 1);

        List<Teaches> list = Arrays.asList(teaches, teaches1, teaches2);


        when(teachesRepository.findAllByLecturerId(1)).thenReturn(list);
        List<Long> courseIds = courseService.getCourseIdsForLecturer(1);
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L), courseIds);
        verify(teachesRepository, Mockito.times(1)).findAllByLecturerId(1);

    }

    @Test
    void testGetCourseIdForLecturerButIsNull() {
        when(teachesRepository.findAllByLecturerId(1)).thenReturn(null);
        Assert.assertNull(courseService.getCourseIdsForLecturer(1));
        verify(teachesRepository, Mockito.times(1)).findAllByLecturerId(1);

    }

    @Test
    void testLecturerTeachesCourse() {
        when(teachesRepository.findById(new TeachesPk(1L, 1L))).thenReturn(Optional.of(teaches));
        Assert.assertTrue(courseService.lecturerTeachesCourse(1, 1));
        verify(teachesRepository, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void testLecturerDoesNotTeachCourse() {
        when(teachesRepository.findById(new TeachesPk(1L, 1L)))
                .thenReturn(Optional.ofNullable(null));
        Assert.assertFalse(courseService.lecturerTeachesCourse(1, 1));
        verify(teachesRepository, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void testCreateTeachesAddNewTeachesToRepo() {
        Boolean result = courseService.createTeaches(1, 1);
        verify(teachesRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertTrue(result);
    }

    @Test
    void testCreateTeachesFailedToAddNewTeach() {
        doThrow(IllegalArgumentException.class).when(teachesRepository).save(Mockito.any());
        Boolean result = courseService.createTeaches(1, 1);
        verify(teachesRepository, Mockito.times(1)).save(Mockito.any());
        Assert.assertFalse(result);
    }
}
