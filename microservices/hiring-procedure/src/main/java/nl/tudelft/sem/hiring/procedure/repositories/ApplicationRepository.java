package nl.tudelft.sem.hiring.procedure.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findAllByApplicationId(long applicationId);

    List<Application> findAllByUserId(long userId);

    List<Application> findAllByCourseId(long courseId);

    List<Application> findAllBySubmissionDateAfter(LocalDateTime submissionDate);

    List<Application> findAllBySubmissionDateBefore(LocalDateTime submissionDate);

    List<Application> findAllByUserIdAndAndCourseId(long userId, long courseId);

    Optional<Application> findByUserIdAndCourseId(long userId, long courseId);

    @Query("SELECT userId, COUNT(userId) AS times FROM Application"
            + " WHERE userId IN (SELECT userId FROM Application WHERE courseId = :cid)"
            + " AND status = 'ACCEPTED' GROUP BY userId ORDER BY times DESC")
    List<Object[]> findTopByTotalTimesSelected(@Param("cid") long courseId);
}
