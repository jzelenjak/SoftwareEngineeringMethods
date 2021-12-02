package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.Course;
import nl.tudelft.sem.courses.entities.Grade;
import nl.tudelft.sem.courses.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends CrudRepository<Grade, Long> {
    Optional<Grade> findByUserAndCourse(User user, Course course);
}
