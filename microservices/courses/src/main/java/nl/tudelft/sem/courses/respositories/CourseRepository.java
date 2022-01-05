package nl.tudelft.sem.courses.respositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import nl.tudelft.sem.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Override
    Optional<Course> findById(Long along);

    List<Course> findAllByCourseCode(String courseCode);

    @Query("SELECT c FROM Courses c WHERE c.id IN :cids")
    List<Course> findAllByIds(@Param("cids") Set<Long> courseIds);

    @Override
    void deleteById(Long id);
}
