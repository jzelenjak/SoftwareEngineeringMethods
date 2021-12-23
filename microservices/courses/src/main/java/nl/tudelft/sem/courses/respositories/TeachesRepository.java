package nl.tudelft.sem.courses.respositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.courses.entities.Teaches;
import nl.tudelft.sem.courses.entities.TeachesPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachesRepository extends JpaRepository<Teaches, TeachesPk> {

    List<Teaches> findAllByLecturerId(long lecturerId);

    @Override
    Optional<Teaches> findById(TeachesPk teachesPk);
}
