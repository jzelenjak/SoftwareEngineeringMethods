package nl.tudelft.sem.hiring.procedure.repositories;

import java.time.LocalDateTime;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query
    List<Application> findAllByApplicationId(long applicationId);

    @Query
    List<Application> findAllByUserId(long userId);

    @Query
    List<Application> findAllByCourseId(long courseId);

    @Query
    List<Application> findAllBySubmissionDateAfter(LocalDateTime submissionDate);

    @Query
    List<Application> findAllBySubmissionDateBefore(LocalDateTime submissionDate);

    @Query
    List<Application> findAllByUserIdAndAndCourseId(long userId, long courseId);
}
