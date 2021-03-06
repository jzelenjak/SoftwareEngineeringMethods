package nl.tudelft.sem.hiring.procedure.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findAllBySubmissionId(long submissionId);

    List<Submission> findAllByUserId(long userId);

    List<Submission> findAllByCourseId(long courseId);

    List<Submission> findAllBySubmissionDateAfter(LocalDateTime submissionDate);

    List<Submission> findAllBySubmissionDateBefore(LocalDateTime submissionDate);

    List<Submission> findAllByUserIdAndAndCourseId(long userId, long courseId);

    List<Submission> findAllByUserIdAndStatus(long userId, SubmissionStatus inProgress);

    Optional<Submission> findByUserIdAndCourseId(long userId, long courseId);


    /**
     * Finds the specified number of applicants (user IDs) for the given course
     * together with the number of times they have been selected for a TA position
     * for any course and having this number higher than `minValue`.
     *
     * @param courseId the id of the course for which students apply
     * @param minValue minimum value for the total count
     * @param pageable basically, a way to specify LIMIT clause
     * @return the list of user IDs and the corresponding number of total times selected
     */
    @Query("SELECT userId, COUNT(userId) AS times FROM Submission"
        + " WHERE userId IN (SELECT userId FROM Submission WHERE courseId = :cid)"
        + " AND status = 2 GROUP BY userId HAVING COUNT(userId) >= :minValue"
        + " ORDER BY times DESC")
    List<Object[]> findTopByTotalTimesSelected(@Param("cid") long courseId, long minValue,
                                               Pageable pageable);

    /**
     * Finds the specified number of applicants (user IDs) for the given course
     * together with the number of times they have been selected for a TA position
     * for the given course and having this number higher than `minValue`.
     *
     * @param courseId  the id of the course for which students apply
     * @param courseIds the ids of the courses with the same course code as the courseId above
     * @param minValue  minimum value for the count
     * @param pageable  basically, a way to specify LIMIT clause
     * @return the list of user IDs and the corresponding number of times selected
     */
    @Query("SELECT userId, COUNT(userId) AS times FROM Submission"
        + " WHERE courseId IN :cids AND userId IN"
        + " (SELECT userId FROM Submission WHERE courseId = :cid)"
        + " AND status = 2 GROUP BY userId HAVING COUNT(userId) >= :minValue"
        + " ORDER BY times DESC")
    List<Object[]> findTopByTimesSelected(@Param("cid") long courseId,
                                          @Param("cids") List<Long> courseIds, long minValue,
                                          Pageable pageable);

    /**
     * Finds all applicants (userIds) for the given course.
     *
     * @param courseId the id of the course for which students apply
     * @return the list of applicants (userIds) for the given course
     */
    @Query("SELECT userId FROM Submission WHERE courseId = :cid")
    List<Long> findAllApplicantsByCourseId(@Param("cid") long courseId);
}
