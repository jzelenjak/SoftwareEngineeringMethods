package nl.tudelft.sem.hour.management.repositories;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HourDeclarationRepository extends JpaRepository<HourDeclaration, Long> {
    List<HourDeclaration> findByStudentId(long studentId);

    List<HourDeclaration> findByApproved(boolean approved);

    @Query("SELECT SUM(declaredHours) FROM HourDeclaration "
            + "WHERE studentId = :studentId AND courseId = :courseId")
    Optional<Double> aggregateHoursFor(@Param("studentId") long studentId,
                                       @Param("courseId") long courseId);
}
