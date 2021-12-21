package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.Teaches;
import nl.tudelft.sem.courses.entities.TeachesPk;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachesRepository extends CrudRepository<Teaches, TeachesPk> {

    List<Teaches> findAllByLecturerId(long lecturerId);

    @Override
    Optional<Teaches> findById(TeachesPk teachesPk);
}
