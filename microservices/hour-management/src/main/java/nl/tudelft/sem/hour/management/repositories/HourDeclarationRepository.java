package nl.tudelft.sem.hour.management.repositories;

import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourDeclarationRepository extends JpaRepository<HourDeclaration, Long> {
}
