package nl.tudelft.sem.courses.respositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.entities.Course;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface CourseRepository extends CrudRepository<Course, Long> {

    Optional<Course> findByCourseId(long courseId);


    List<Course> findAllByCourseCode(String courseCode);


    @Override
    void deleteById(Long id);
}
