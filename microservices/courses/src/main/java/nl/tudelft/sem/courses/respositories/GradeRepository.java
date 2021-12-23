package nl.tudelft.sem.courses.respositories;


import java.util.Optional;
import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {


    Optional<Grade> findByUserIdAndCourse(long userId, Course course);
}
