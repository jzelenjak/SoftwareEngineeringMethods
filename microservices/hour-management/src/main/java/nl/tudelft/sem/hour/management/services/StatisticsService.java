package nl.tudelft.sem.hour.management.services;

import java.util.Optional;
import lombok.Data;
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
}
