package nl.tudelft.sem.hiring.procedure.services;

import java.time.LocalDateTime;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private final transient ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Function for checking if there are any Applications with the same user ID and course ID
     * that were submitted in the same year.
     *
     * @param userId The ID of the user
     * @param courseId The ID of the course
     * @return true if there are no overlapping Applications, false otherwise
     */
    public boolean checkSameApplication(long userId, long courseId) {
        List<Application> applications = applicationRepository
            .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Application application : applications) {
            if (application.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Function for checking if the application is within the allowed deadline of 3 weeks before
     * the course starts.
     *
     * @param courseStart The LocalDateTime at which the course starts
     * @return true if the application is within the deadline, false otherwise
     */
    public boolean checkDeadline(LocalDateTime courseStart) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime deadline = courseStart.minusWeeks(3);
        return currentTime.isBefore(deadline);
    }

    /**
     * Function for creating an Application object and saving it into the database.
     *
     * @param userId The ID of the user
     * @param courseId The ID of the course
     */
    public void createApplication(long userId, long courseId) {
        LocalDateTime currentTime = LocalDateTime.now();
        Application application = new Application(userId, courseId, currentTime);
        applicationRepository.save(application);
    }
}
