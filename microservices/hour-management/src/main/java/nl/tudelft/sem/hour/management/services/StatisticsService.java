package nl.tudelft.sem.hour.management.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import nl.tudelft.sem.hour.management.dto.StudentHoursTuple;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
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
                .aggregateByCourseIdSetAndStudentIdSet(studentIds, courseIds, minHours)
                .stream()
                .limit(amount);
    }

    /**
     * Calculates the mean of a collection of declared hours.
     *
     * @param hourDeclarationCollection collection containing hour declarations.
     * @return mean of the hour declarations stored in collection.
     */
    public double calculateMean(Collection<HourDeclaration> hourDeclarationCollection) {
        // Use reduce to calculate the hourDeclaration total

        // Does not use the getTotalHoursPerStudentPerCourse
        // method to avoid additional repository accesses
        Optional<Double> result = hourDeclarationCollection
                .stream()
                .map(HourDeclaration::getDeclaredHours)
                .reduce(Double::sum);

        // If empty, return a special value
        if (result.isEmpty()) {
            return -1;
        }

        return result.get() / hourDeclarationCollection.size();
    }

    /**
     * Calculates the median of a collection of declared hours.
     *
     * @param hourDeclarationCollection collection containing hour declarations.
     * @return median of the hour declarations stored in collection.
     */
    public double calculateMedian(Collection<HourDeclaration> hourDeclarationCollection) {
        // Assumes that the collection is already sorted
        List<Double> hourDeclarations = hourDeclarationCollection
                .stream()
                .map(HourDeclaration::getDeclaredHours)
                .collect(Collectors.toList());

        // If empty, return a special value
        if (hourDeclarations.isEmpty()) {
            return -1;
        }

        int size = hourDeclarations.size();

        // Check whether it is even or odd
        if (size % 2 == 0) {
            return (hourDeclarations.get(size / 2 - 1) + hourDeclarations.get(size / 2)) / 2;
        } else {
            return hourDeclarations.get(size / 2);
        }
    }

    /**
     * Calculates the standard deviation of a collection of declared hours.
     *
     * @param mean mean value of the values inside collection.
     * @param hourDeclarationCollection collection containing hour declarations.
     * @return standard deviation of the hour declarations stored in collection.
     */
    public double calculateStandardDeviation(double mean, Collection<HourDeclaration>
            hourDeclarationCollection) {
        Optional<Double> result = hourDeclarationCollection
                .stream()
                .map(v -> Math.pow(v.getDeclaredHours() - mean, 2.0))
                .reduce(Double::sum);

        if (result.isEmpty()) {
            return -1;
        }

        return Math.sqrt(result.get() / hourDeclarationCollection.size());
    }

}
