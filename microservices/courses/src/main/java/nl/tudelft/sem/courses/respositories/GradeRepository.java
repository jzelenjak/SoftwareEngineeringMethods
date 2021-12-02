package nl.tudelft.sem.courses.respositories;

import nl.tudelft.sem.courses.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}
