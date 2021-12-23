package nl.tudelft.sem.courses.respositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    @Override
    Optional<Course> findById(Long along);

    List<Course> findAllByCourseCode(String courseCode);


    @Override
    void deleteById(Long id);
}
