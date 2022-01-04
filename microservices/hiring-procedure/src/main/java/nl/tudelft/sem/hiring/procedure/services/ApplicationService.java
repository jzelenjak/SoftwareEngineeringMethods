package nl.tudelft.sem.hiring.procedure.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
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
     * @param userId   The ID of the user
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
     * @param userId   The ID of the user
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
     * Returns the application associated to the id.
     *
     * @param applicationId is the id of the application.
     * @return the application associated to the id, if exists.
     */
    public Optional<Application> getApplication(long applicationId) {
        return applicationRepository.findById(applicationId);
    }

    /**
     * Returns the optional application associated to the user and course.
     * There is only one application per user per course at most.
     *
     * @param userId   is the ID of the user.
     * @param courseId is the ID of the course.
     * @return the application associated to the user and course, if exists.
     */
    public Optional<Application> getApplication(long userId, long courseId) {
        return applicationRepository.findByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Finds all the unreviewed applications for a particular user.
     *
     * @param userId is the ID of the user.
     * @return a list of all the unreviewed applications for the given user.
     */
    public List<Application> getUnreviewedApplicationsForUser(long userId) {
        return applicationRepository.findAllByUserIdAndStatus(userId,
                ApplicationStatus.IN_PROGRESS);
    }

    /**
     * Function for updating the status of an application to be rejected.
     *
     * @param applicationId is the ID of the application.
     */
    public void rejectApplication(long applicationId) {
        applicationRepository.findById(applicationId).ifPresent(application -> {
            application.setStatus(ApplicationStatus.REJECTED);
            applicationRepository.save(application);
        });
    }

    /**
     * Function for updating the status of an application to be withdrawn.
     *
     * @param applicationId is the ID of the application.
     */
    public void withdrawApplication(long applicationId) {
        applicationRepository.findById(applicationId).ifPresent(application -> {
            application.setStatus(ApplicationStatus.WITHDRAWN);
            applicationRepository.save(application);
        });
    }

    /**
     * Checks whether a user is a viable candidate to that course. This is the case when they have
     * applied and their application is "in progress".
     *
     * @param userId   The ID of the user to be checked
     * @param courseId The ID of the course for which the user should be checked
     * @return true if the user is a viable candidate, false otherwise
     */
    public boolean checkCandidate(long userId, long courseId) {
        List<Application> applications = applicationRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Application application : applications) {
            if (application.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                return application.getStatus() == ApplicationStatus.IN_PROGRESS;
            }
        }
        return false;
    }

    /**
     * Function for setting the status of the most recent application of a user to a course to
     * "Accepted".
     *
     * @param userId   The ID of the user to be hired
     * @param courseId The ID of the course that the user should be hired to
     */
    public void hire(long userId, long courseId) {
        List<Application> applications = applicationRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Application application : applications) {
            if (application.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                application.setStatus(ApplicationStatus.ACCEPTED);
                applicationRepository.save(application);
            }
        }
    }

    /**
     * Method for setting the maximum amount of allowed contractual hours for an application.
     *
     * @param applicationId The ID of the application for which to change the maximum allowed hours
     * @param maxHours      The amount to which to update
     * @throws NoSuchElementException when an application with that associated id does not exist.
     */
    public void setMaxHours(long applicationId, int maxHours) throws NoSuchElementException {
        Optional<Application> applicationOptional = applicationRepository.findById(applicationId);
        if (applicationOptional.isPresent()) {
            Application application = applicationOptional.get();
            application.setMaxHours(maxHours);
            applicationRepository.save(application);
        } else {
            throw new NoSuchElementException("Application with that id does not exist.");
        }
    }

    /**
     * Method for getting the maximum amount of allowed contractual hours for an application.
     *
     * @param userId   The ID of the user for which to get the maximum allowed hours
     * @param courseId The ID of the course for which to get the maximum allowed hours
     * @return The maxHours of the application with those two parameters
     * @throws NoSuchElementException when no applications with those parameters exist
     */
    public int getMaxHours(long userId, long courseId) throws NoSuchElementException {
        List<Application> applications = applicationRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        if (applications.size() != 0) {
            Application application = applications.get(0);
            return application.getMaxHours();
        }
        throw new NoSuchElementException("Application with those parameters does not exist.");
    }


    /**
     * Method for setting the rating of an approved application.
     *
     * @param applicationId The ID of the application for which to set the rating
     * @param rating        The value of the rating
     * @throws IllegalStateException  if the application has not been approved
     * @throws NoSuchElementException if an application with those parameters does not exist.
     */
    public void setRating(long applicationId, double rating)
            throws IllegalStateException, NoSuchElementException {
        Optional<Application> applicationOptional = applicationRepository.findById(applicationId);
        if (applicationOptional.isPresent()) {
            Application application = applicationOptional.get();
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                throw new IllegalStateException("Application is not approved.");
            }
            application.setRating(rating);
            applicationRepository.save(application);
        } else {
            throw new NoSuchElementException("Application with that id does not exist.");
        }
    }

    /**
     * Method for getting the rating of an approved application.
     *
     * @param userId   The ID of the user for which to get the rating
     * @param courseId The ID of the course for which to get the rating
     * @return The rating of the TA
     * @throws IllegalStateException  if the application has not been approved
     *                                or has not been rated yet.
     * @throws NoSuchElementException if an application with those parameters does not exist.
     */
    public double getRating(long userId, long courseId)
            throws IllegalStateException, NoSuchElementException {
        List<Application> applications = applicationRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        if (applications.size() != 0) {
            Application application = applications.get(0);
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                throw new IllegalStateException("Application is not approved.");
            }
            if (application.getRating() == -1.0) {
                throw new IllegalStateException("Application is not yet rated.");
            }
            return application.getRating();
        }
        throw new NoSuchElementException("Application with those parameters does not exist.");
    }
}
