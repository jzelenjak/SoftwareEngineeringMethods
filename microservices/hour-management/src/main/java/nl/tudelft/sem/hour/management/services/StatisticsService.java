package nl.tudelft.sem.hour.management.services;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Data;
import nl.tudelft.sem.hour.management.dto.StudentHoursTuple;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import org.springframework.stereotype.Service;

@Data
@Service
public class StatisticsService {
    private final HourDeclarationRepository hourDeclarationRepository;

    /**
     * Get the total amount of hours declared for a student per course.
     *
     * @param studentId the student id.
     * @param courseId  the course id.
     * @return the total amount of hours declared.
     */
    public Optional<Double> getTotalHoursPerStudentPerCourse(long studentId, long courseId) {
        return hourDeclarationRepository.aggregateHoursFor(studentId, courseId);
    }

    /**
     * Gets the total amount of hours spent by a requested
     * student on any of the courses (aggregated).
     *
     * @param studentIds is a list of student ids.
     * @param courseIds  is a list of course ids.
     * @param minHours   is the minimum amount of approved hours a student must have declared.
     * @param amount     is the amount of entries to be returned.
     * @return a stream of amount tuples.
     */
    public Stream<StudentHoursTuple> getTotalHoursPerStudentPerCourse(
            Set<Long> studentIds, Set<Long> courseIds, double minHours, int amount) {
        // Use streams and FP to allow lazy operations
        return hourDeclarationRepository
                .findByCourseIdSetAndStudentIdSet(studentIds, courseIds, minHours)
                .stream()
                .limit(amount);
    }

}
