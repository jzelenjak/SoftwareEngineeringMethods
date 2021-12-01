package nl.tudelft.sem.hiring.procedure.repositories;

import java.util.List;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query
    List<Application> findAllByApplicationId();

    @Query
    List<Application> findAllByUserId();

    @Query
    List<Application> findAllByCourseId();

    @Query
    List<Application> findAllBySubmissionDateAfter();

    @Query
    List<Application> findAllBySubmissionDateBefore();
}
