package nl.tudelft.sem.hiring.procedure.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {
    @Mock
    private transient SubmissionRepository submissionRepository;

    @InjectMocks
    private transient SubmissionService submissionService;

    private transient Submission submission1;
    private transient Submission submission2;
    private transient Submission submission3;
    private transient Submission submission4;

    private transient LocalDateTime now;

    private final transient long userId1 = 521234;
    private final transient long courseId1 = 2450;
    private final transient long courseId2 = 2460;

    /**
     * Setup for each test.
     */
    @BeforeEach
    public void beforeEach() {
        now = LocalDateTime.now();
        LocalDateTime yearAgo = now.minusYears(1);
        submission1 = new Submission(userId1, courseId1, now);
        submission2 = new Submission(userId1, courseId1, yearAgo);
        submission3 = new Submission(userId1, courseId2, now);
        submission4 = new Submission(userId1, courseId2, now);
    }

    @Test
    public void testCreateApplication() {
        submissionService.createSubmission(userId1, courseId1, now);
        verify(submissionRepository).save(submission1);
    }

    @Test
    public void testCheckSameApplicationTrue() {
        given(submissionRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(submission2));
        boolean actual = submissionService.checkSameSubmission(userId1,
                courseId1);
        assertTrue(actual);
    }

    @Test
    public void testCheckSameApplicationFalse() {
        given(submissionRepository.findAllByUserIdAndAndCourseId(userId1, courseId2)).willReturn(
                List.of(submission4));
        boolean actual = submissionService.checkSameSubmission(userId1,
                courseId2);
        assertFalse(actual);
    }

    @Test
    public void testCheckDeadlineTrue() {
        LocalDateTime fourWeeks = now.plusWeeks(4);
        boolean actual = submissionService.checkDeadline(fourWeeks);
        assertTrue(actual);
    }

    @Test
    public void testCheckDeadlineFalse() {
        LocalDateTime twoWeeks = now.minusWeeks(2);
        boolean actual = submissionService.checkDeadline(twoWeeks);
        assertFalse(actual);
    }

    @Test
    public void testGetApplicationsForCourse() {
        given(submissionRepository.findAllByCourseId(courseId1)).willReturn(
                List.of(submission1, submission2));
        List<Submission> actual = submissionService.getSubmissionsForCourse(courseId1);
        List<Submission> expected = List.of(submission1, submission2);
        assertEquals(actual, expected);
    }

    @Test
    public void testGetUnreviewedApplicationsForUser() {
        given(submissionRepository.findAllByUserIdAndStatus(userId1,
                SubmissionStatus.IN_PROGRESS)).willReturn(List.of(submission1, submission2));
        List<Submission> actual = submissionService.getUnreviewedSubmissionsForUser(userId1);
        List<Submission> expected = List.of(submission1, submission2);
        assertEquals(actual, expected);
    }

    @Test
    public void testGetAllApplications() {
        given(submissionRepository.findAll()).willReturn(
                List.of(submission1, submission2, submission3, submission4));
        List<Submission> actual = submissionService.getAllSubmissions();
        List<Submission> expected = List.of(submission1, submission2,
            submission3, submission4);
        assertEquals(actual, expected);
    }

    @Test
    public void testCheckCandidateTrue() {
        given(submissionRepository.findById(submission1.getSubmissionId()))
                .willReturn(Optional.of(submission1));
        boolean actual = submissionService.checkCandidate(submission1.getSubmissionId());
        assertTrue(actual);
    }

    @Test
    public void testCheckCandidateFalse() {
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        given(submissionRepository.findById(submission1.getSubmissionId()))
                .willReturn(Optional.of(submission1));
        boolean actual = submissionService.checkCandidate(submission1.getSubmissionId());
        assertFalse(actual);
        submission1.setStatus(SubmissionStatus.IN_PROGRESS);
    }

    @Test
    public void testCheckCandidateNoApplications() {
        given(submissionRepository.findById(submission2.getSubmissionId()))
                .willReturn(Optional.empty());
        boolean actual = submissionService.checkCandidate(submission2.getSubmissionId());
        assertFalse(actual);
    }

    @Test
    public void testHireGoesThrough() {
        given(submissionRepository.findById(submission1.getSubmissionId()))
                .willReturn(Optional.of(submission1));
        submissionService.hire(submission1.getSubmissionId());
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        verify(submissionRepository).save(submission1);
    }

    @Test
    public void testHireFails() {
        given(submissionRepository.findById(submission2.getSubmissionId()))
                .willReturn(Optional.of(submission2));
        submissionService.hire(submission2.getSubmissionId());
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        verify(submissionRepository, never()).save(submission1);
    }

    @Test
    public void getApplicationTest() {
        given(submissionRepository.findByUserIdAndCourseId(userId1, courseId1))
                .willReturn(Optional.of(submission1));
        Optional<Submission> actual = submissionService.getSubmission(userId1, courseId1);
        assertTrue(actual.isPresent());
        assertEquals(submission1, actual.get());
        verify(submissionRepository).findByUserIdAndCourseId(userId1, courseId1);
    }

    @Test
    public void testGetApplicationById() {
        given(submissionRepository.findById(submission1.getSubmissionId())).willReturn(
                Optional.of(submission1));
        Optional<Submission> actual = submissionService.getSubmission(
                submission1.getSubmissionId());
        assertTrue(actual.isPresent());
        assertEquals(submission1, actual.get());
        verify(submissionRepository).findById(submission1.getSubmissionId());
    }

    @Test
    public void testWithdrawApplication() {
        Submission submissionMock = Mockito.mock(Submission.class);
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.of(submissionMock));

        submissionService.withdrawSubmission(submission1.getSubmissionId());
        verify(submissionMock, times(1)).setStatus(SubmissionStatus.WITHDRAWN);
        verify(submissionRepository).save(submissionMock);
    }

    @Test
    public void testRejectApplication() {
        Submission submissionMock = Mockito.mock(Submission.class);
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.of(submissionMock));

        submissionService.rejectSubmission(submission1.getSubmissionId());
        verify(submissionMock, times(1)).setStatus(SubmissionStatus.REJECTED);
        verify(submissionRepository).save(submissionMock);
    }

    @Test
    public void testSetMaxHoursPass() {
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.of(submission1));

        submissionService.setMaxHours(submission1.getSubmissionId(), 150);
        submission1.setMaxHours(150);

        verify(submissionRepository).save(submission1);
    }

    @Test
    public void testSetMaxHoursFail() {
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                submissionService.setMaxHours(submission1.getSubmissionId(), 150));
    }

    @Test
    public void testGetMaxHoursPass() {
        when(submissionRepository.findAllByUserIdAndAndCourseId(submission1.getUserId(),
                submission1.getCourseId())).thenReturn(List.of(submission1));

        assertEquals(submissionService.getMaxHours(submission1.getUserId(),
                submission1.getCourseId()), 200);
    }

    @Test
    public void testGetMaxHoursFail() {
        when(submissionRepository.findAllByUserIdAndAndCourseId(submission1.getUserId(),
                submission1.getCourseId())).thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () ->
                submissionService.getMaxHours(submission1.getUserId(),
                        submission1.getCourseId()));
    }

    @Test
    public void testSetRatingPass() {
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.of(submission1));

        submissionService.setRating(submission1.getSubmissionId(), 7.5);

        assertEquals(submission1.getRating(), 7.5);
        verify(submissionRepository).save(submission1);
    }

    @Test
    public void testSetRatingNotApproved() {
        submission1.setStatus(SubmissionStatus.REJECTED);
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.of(submission1));

        assertThrows(IllegalStateException.class,
                () -> submissionService.setRating(submission1.getSubmissionId(), 7.5));
    }

    @Test
    public void testSetRatingNoApplication() {
        submission1.setStatus(SubmissionStatus.REJECTED);
        when(submissionRepository.findById(submission1.getSubmissionId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> submissionService.setRating(submission1.getSubmissionId(), 7.5));
    }

    @Test
    public void testGetRatingPass() {
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        submission1.setRating(7.5);
        when(submissionRepository
                .findAllByUserIdAndAndCourseId(submission1.getUserId(),
                        submission1.getCourseId()))
                .thenReturn(List.of(submission1));

        assertEquals(submissionService
                        .getRating(submission1.getUserId(), submission1.getCourseId()),
                7.5);
    }

    @Test
    public void testGetRatingNotApproved() {
        submission1.setStatus(SubmissionStatus.REJECTED);
        when(submissionRepository
                .findAllByUserIdAndAndCourseId(submission1.getUserId(),
                        submission1.getCourseId()))
                .thenReturn(List.of(submission1));

        assertThrows(IllegalStateException.class, () -> submissionService
                .getRating(submission1.getUserId(), submission1.getCourseId()));
    }

    @Test
    public void testGetRatingNotRated() {
        submission1.setStatus(SubmissionStatus.ACCEPTED);
        when(submissionRepository
                .findAllByUserIdAndAndCourseId(submission1.getUserId(),
                        submission1.getCourseId()))
                .thenReturn(List.of(submission1));

        assertThrows(IllegalStateException.class, () -> submissionService
                .getRating(submission1.getUserId(), submission1.getCourseId()));
    }

    @Test
    public void testGetRatingNoApplication() {
        when(submissionRepository
                .findAllByUserIdAndAndCourseId(submission1.getUserId(),
                        submission1.getCourseId()))
                .thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () -> submissionService
                .getRating(submission1.getUserId(), submission1.getCourseId()));
    }

    @Test
    public void testGetApplicationsForStudent() {
        when(submissionRepository.findAllByUserId(submission1.getUserId()))
                .thenReturn(List.of(submission1, submission2,
                    submission3, submission4));
        submissionService.getSubmissionsForStudent(submission1.getUserId());

        verify(submissionRepository).findAllByUserId(submission1.getUserId());
    }

}
