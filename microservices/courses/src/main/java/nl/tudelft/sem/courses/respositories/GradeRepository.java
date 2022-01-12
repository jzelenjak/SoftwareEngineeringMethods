package nl.tudelft.sem.courses.respositories;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nl.tudelft.sem.courses.communication.StudentGradeTuple;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByUserIdAndCourse(long userId, Course course);

    @Query("SELECT new nl.tudelft.sem.courses"
            + ".communication.StudentGradeTuple(userId, gradeValue) "
            + "FROM Grades "
            + "WHERE user_id in :userIds AND grade >= :minGrade AND course.id IN :courses "
            + "ORDER BY grade DESC")
    Collection<StudentGradeTuple> getMultipleUserGrades(
            @Param("userIds") List<Long> userIds,
            @Param("minGrade") float minGrade,
            @Param("courses") List<Long> courseIds);
}
