package nl.tudelft.sem.hour.management.repositories;

import java.util.List;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourDeclarationRepository extends JpaRepository<HourDeclaration, Long> {
    List<HourDeclaration> findByStudentId(long studentId);

    List<HourDeclaration> findByApproved(boolean approved);
}
