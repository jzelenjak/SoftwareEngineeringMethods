package nl.tudelft.sem.courses.respositories;

import java.util.Optional;
import nl.tudelft.sem.courses.entities.Course;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface CourseRepository extends CrudRepository<Course, Long> {

    Optional<Course> findByCourseId(String courseId);

    @Override
    void deleteById(Long id);
}
