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
    public void createApplication(long userId, long courseId, LocalDateTime currentTime) {
        Application application = new Application(userId, courseId, currentTime);
        applicationRepository.save(application);
    }

    public List<Application> getApplicationsForCourse(long courseId) {
        return applicationRepository.findAllByCourseId(courseId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    /**
     * Checks whether a user is a viable candidate to that course. This is the case when they have
     * applied and their application is "in progress".
     *
     * @param userId The ID of the user to be checked
     * @param courseId The ID of the course for which the user should be checked
     * @return true if the user is a viable candidate, false otherwise
     */
    public boolean checkCandidate(long userId, long courseId) {
        List<Application> applications = applicationRepository
            .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Application application : applications) {
            if (application.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                return application.getStatus() == 0;
            }
        }
        return false;
    }

    /**
     * Function for setting the status of the most recent application of a user to a course to
     * "Accepted".
     *
     * @param userId The ID of the user to be hired
     * @param courseId The ID of the course that the user should be hired to
     */
    public void hire(long userId, long courseId) {
        List<Application> applications = applicationRepository
            .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Application application : applications) {
            if (application.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                application.setStatus(2);
                applicationRepository.save(application);
            }
        }
    }
}
