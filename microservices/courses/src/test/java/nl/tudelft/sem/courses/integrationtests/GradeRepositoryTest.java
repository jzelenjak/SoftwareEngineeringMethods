package nl.tudelft.sem.courses.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import nl.tudelft.sem.courses.communication.StudentGradeTuple;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GradeRepositoryTest {
    @Autowired
    private transient CourseRepository courseRepository;

    @Autowired
    private transient GradeRepository gradeRepository;

    private final transient ZonedDateTime date = ZonedDateTime.now();

    private transient Course course;
    private transient Course course2;

    private transient StudentGradeTuple gradeTuple1;
    private transient StudentGradeTuple gradeTuple2;
    private transient StudentGradeTuple gradeTuple3;

    @BeforeAll
    void init() {
        //required objects for this test
        course = new Course(1, "CSE1011", date, date, 3);
        course2 = new Course(2, "CSE1011", date, date, 3);

        courseRepository.save(course);
        courseRepository.save(course2);

        Grade gradeUser1 = new Grade(1, course, 1, 7.6F);
        Grade gradeUser2 = new Grade(2, course2, 2, 9.9F);
        Grade gradeUser3 = new Grade(3, course, 3, 2.0F);
        Grade gradeUser4 = new Grade(4, course, 4, 7F);

        gradeRepository.save(gradeUser1);
        gradeRepository.save(gradeUser2);
        gradeRepository.save(gradeUser3);
        gradeRepository.save(gradeUser4);

        gradeTuple1 = new StudentGradeTuple(2L, 9.9F);
        gradeTuple2 = new StudentGradeTuple(1L, 7.6F);
        gradeTuple3 = new StudentGradeTuple(4L, 7F);
    }

    @Test
    void testGetMultipleUserGradesValid() {
        Collection<StudentGradeTuple> result = gradeRepository
                .getMultipleUserGrades(List.of(1L, 2L, 3L, 4L), 7,
                        List.of(course.getId(), course2.getId()));

        Collection<StudentGradeTuple> expected = List.of(gradeTuple1, gradeTuple2, gradeTuple3);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGetMultipleUserGradesMinGradeTooHigh() {
        Collection<StudentGradeTuple> result = gradeRepository
                .getMultipleUserGrades(List.of(1L, 2L, 3L, 4L),
                        9.9F, List.of(course2.getId()));

        Collection<StudentGradeTuple> expected = List.of(gradeTuple1);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGetMultipleUserGradesTestEmptyUser() {
        Collection<StudentGradeTuple> result = gradeRepository
                .getMultipleUserGrades(List.of(),
                        7F, List.of(course.getId()));

        Collection<StudentGradeTuple> expected = List.of();

        assertThat(result).isEqualTo(expected);
    }
}
