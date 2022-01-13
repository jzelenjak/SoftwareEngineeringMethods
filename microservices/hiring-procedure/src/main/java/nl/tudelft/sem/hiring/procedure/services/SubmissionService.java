package nl.tudelft.sem.hiring.procedure.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.swing.text.html.Option;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class SubmissionService {
    private final transient SubmissionRepository submissionRepository;

    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    /**
     * Function for checking if there are any Submissions with the same user ID and course ID
     * that were submitted in the same year.
     *
     * @param userId   The ID of the user
     * @param courseId The ID of the course
     * @return true if there are no overlapping Submissions, false otherwise
     */
    public boolean checkSameSubmission(long userId, long courseId) {
        List<Submission> submissions = submissionRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        for (Submission submission : submissions) {
            if (submission.getSubmissionDate().getYear() == LocalDateTime.now().getYear()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Function for checking if the submission is within the allowed deadline of 3 weeks before
     * the course starts.
     *
     * @param courseStart The LocalDateTime at which the course starts
     * @return true if the submission is within the deadline, false otherwise
     */
    public boolean checkDeadline(LocalDateTime courseStart) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime deadline = courseStart.minusWeeks(3);
        return currentTime.isBefore(deadline);
    }

    /**
     * Function for creating a Submission object and saving it into the database.
     *
     * @param userId   The ID of the user
     * @param courseId The ID of the course
     */
    public void createSubmission(long userId, long courseId, LocalDateTime currentTime) {
        Submission submission = new Submission(userId, courseId, currentTime);
        submissionRepository.save(submission);
    }

    public List<Submission> getSubmissionsForCourse(long courseId) {
        return submissionRepository.findAllByCourseId(courseId);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    /**
     * Returns the submission associated to the id.
     *
     * @param submissionId is the id of the submission.
     * @return the submission associated to the id, if exists. Null otherwise.
     */
    public Submission getSubmission(long submissionId) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
        return optionalSubmission.orElse(null);
    }

    /**
     * Returns the optional submission associated to the user and course.
     * There is only one submission per user per course at most.
     *
     * @param userId   is the ID of the user.
     * @param courseId is the ID of the course.
     * @return the submission associated to the user and course, if exists. Null otherwise.
     */
    public Submission getSubmission(long userId, long courseId) {
        Optional<Submission> optionalSubmission =
            submissionRepository.findByUserIdAndCourseId(userId, courseId);
        return optionalSubmission.orElse(null);
    }

    public List<Submission> getSubmissionsForStudent(long userId) {
        return submissionRepository.findAllByUserId(userId);
    }

    /**
     * Finds all the unreviewed submissions for a particular user.
     *
     * @param userId is the ID of the user.
     * @return a list of all the unreviewed submissions for the given user.
     */
    public List<Submission> getUnreviewedSubmissionsForUser(long userId) {
        return submissionRepository.findAllByUserIdAndStatus(userId,
                SubmissionStatus.IN_PROGRESS);
    }

    /**
     * Function for updating the status of a submission to be rejected.
     *
     * @param submissionId is the ID of the submission.
     */
    public void rejectSubmission(long submissionId) {
        submissionRepository.findById(submissionId).ifPresent(submission -> {
            submission.setStatus(SubmissionStatus.REJECTED);
            submissionRepository.save(submission);
        });
    }

    /**
     * Function for updating the status of a submission to be withdrawn.
     *
     * @param submissionId is the ID of the submission.
     */
    public void withdrawSubmission(long submissionId) {
        submissionRepository.findById(submissionId).ifPresent(submission -> {
            submission.setStatus(SubmissionStatus.WITHDRAWN);
            submissionRepository.save(submission);
        });
    }

    /**
     * Checks whether a user is a viable candidate to that course. This is the case when they have
     * applied and their submission is "in progress".
     *
     * @param submissionId is the ID of the submission that needs to be checked.
     * @return true if the user is a viable candidate, false otherwise
     */
    public boolean checkCandidate(long submissionId) {
        Optional<Submission> submission = submissionRepository.findById(submissionId);
        return submission
                .filter(value -> value.getStatus() == SubmissionStatus.IN_PROGRESS)
                .isPresent();
    }

    /**
     * Function for setting the status of a submission to "Accepted".
     *
     * @param submissionId is the id of the submission to be accepted.
     */
    public void hire(long submissionId) {
        submissionRepository.findById(submissionId).ifPresent(submission -> {
            submission.setStatus(SubmissionStatus.ACCEPTED);
            submissionRepository.save(submission);
        });
    }

    /**
     * Method for setting the maximum amount of allowed contractual hours for a submission.
     *
     * @param submissionId The ID of the submission for which to change the maximum allowed hours
     * @param maxHours     The amount to which to update
     * @throws NoSuchElementException when a submission with that associated id does not exist.
     */
    public void setMaxHours(long submissionId, int maxHours) throws NoSuchElementException {
        Optional<Submission> submissionOptional = submissionRepository.findById(submissionId);
        if (submissionOptional.isPresent()) {
            Submission submission = submissionOptional.get();
            submission.setMaxHours(maxHours);
            submissionRepository.save(submission);
        } else {
            throw new NoSuchElementException("Submission with that id does not exist.");
        }
    }

    /**
     * Method for getting the maximum amount of allowed contractual hours for a submission.
     *
     * @param userId   The ID of the user for which to get the maximum allowed hours
     * @param courseId The ID of the course for which to get the maximum allowed hours
     * @return The maxHours of the submission with those two parameters
     * @throws NoSuchElementException when no submissions with those parameters exist
     */
    public int getMaxHours(long userId, long courseId) throws NoSuchElementException {
        List<Submission> submissions = submissionRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        if (submissions.size() != 0) {
            Submission submission = submissions.get(0);
            return submission.getMaxHours();
        }
        throw new NoSuchElementException("Submission with those parameters does not exist.");
    }


    /**
     * Method for setting the rating of an approved submission.
     *
     * @param submissionId The ID of the submission for which to set the rating
     * @param rating       The value of the rating
     * @throws IllegalStateException  if the submission has not been approved
     * @throws NoSuchElementException if a submission with those parameters does not exist.
     */
    public void setRating(long submissionId, double rating)
            throws IllegalStateException, NoSuchElementException {
        Optional<Submission> submissionOptional = submissionRepository.findById(submissionId);
        if (submissionOptional.isPresent()) {
            Submission submission = submissionOptional.get();
            if (submission.getStatus() != SubmissionStatus.ACCEPTED) {
                throw new IllegalStateException("Submission is not approved.");
            }
            submission.setRating(rating);
            submissionRepository.save(submission);
        } else {
            throw new NoSuchElementException("Submission with that id does not exist.");
        }
    }

    /**
     * Method for getting the rating of an approved submission.
     *
     * @param userId   The ID of the user for which to get the rating
     * @param courseId The ID of the course for which to get the rating
     * @return The rating of the TA
     * @throws IllegalStateException  if the submission has not been approved
     *                                or has not been rated yet.
     * @throws NoSuchElementException if a submission with those parameters does not exist.
     */
    public double getRating(long userId, long courseId)
            throws IllegalStateException, NoSuchElementException {
        List<Submission> submissions = submissionRepository
                .findAllByUserIdAndAndCourseId(userId, courseId);
        if (submissions.size() != 0) {
            Submission submission = submissions.get(0);
            if (submission.getStatus() != SubmissionStatus.ACCEPTED) {
                throw new IllegalStateException("Submission is not approved.");
            }
            if (submission.getRating() == -1.0) {
                throw new IllegalStateException("Submission is not yet rated.");
            }
            return submission.getRating();
        }
        throw new NoSuchElementException("Submission with those parameters does not exist.");
    }
}
