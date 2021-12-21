package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.Lecturer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturerRepository extends CrudRepository<Lecturer, Long> {

    List<Lecturer> findAllByLecturerId(long lecturerId);

}
