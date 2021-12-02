package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
//import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface CourseRepository extends CrudRepository<Course, Long> {

    Optional<Course> findByCourseID(String courseID);

    @Override
    void deleteById(Long aLong);
}
