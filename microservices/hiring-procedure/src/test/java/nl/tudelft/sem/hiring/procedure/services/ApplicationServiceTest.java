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
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {
    @Mock
    private transient ApplicationRepository applicationRepository;

    @InjectMocks
    private transient ApplicationService applicationService;

    private transient Application application1;
    private transient Application application2;
    private transient Application application3;
    private transient Application application4;

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
        application1 = new Application(userId1, courseId1, now);
        application2 = new Application(userId1, courseId1, yearAgo);
        application3 = new Application(userId1, courseId2, now);
        application4 = new Application(userId1, courseId2, now);
    }

    @Test
    public void createApplicationTest() {
        applicationService.createApplication(userId1, courseId1, now);
        verify(applicationRepository).save(application1);
    }

    @Test
    public void checkSameApplicationTrue() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application2));
        boolean actual = applicationService.checkSameApplication(userId1,
                courseId1);
        assertTrue(actual);
    }

    @Test
    public void checkSameApplicationFalse() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId2)).willReturn(
                List.of(application4));
        boolean actual = applicationService.checkSameApplication(userId1,
                courseId2);
        assertFalse(actual);
    }

    @Test
    public void checkDeadlineTrue() {
        LocalDateTime fourWeeks = now.plusWeeks(4);
        boolean actual = applicationService.checkDeadline(fourWeeks);
        assertTrue(actual);
    }

    @Test
    public void checkDeadlineFalse() {
        LocalDateTime twoWeeks = now.minusWeeks(2);
        boolean actual = applicationService.checkDeadline(twoWeeks);
        assertFalse(actual);
    }

    @Test
    public void getApplicationsForCourseTest() {
        given(applicationRepository.findAllByCourseId(courseId1)).willReturn(
                List.of(application1, application2));
        List<Application> actual = applicationService.getApplicationsForCourse(courseId1);
        List<Application> expected = List.of(application1, application2);
        assertEquals(actual, expected);
    }

    @Test
    public void getAllApplications() {
        given(applicationRepository.findAll()).willReturn(
                List.of(application1, application2, application3, application4));
        List<Application> actual = applicationService.getAllApplications();
        List<Application> expected = List.of(application1, application2,
                application3, application4);
        assertEquals(actual, expected);
    }

    @Test
    public void checkCandidateTrue() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application1, application2));
        boolean actual = applicationService.checkCandidate(userId1, courseId1);
        assertTrue(actual);
    }

    @Test
    public void checkCandidateFalse() {
        application1.setStatus(ApplicationStatus.ACCEPTED);
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application1, application2));
        boolean actual = applicationService.checkCandidate(userId1, courseId1);
        assertFalse(actual);
        application1.setStatus(ApplicationStatus.IN_PROGRESS);
    }

    @Test
    public void checkCandidateNoApplications() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application2));
        boolean actual = applicationService.checkCandidate(userId1, courseId1);
        assertFalse(actual);
    }

    @Test
    public void hireGoesThroughTest() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application1, application2));
        applicationService.hire(userId1, courseId1);
        application1.setStatus(ApplicationStatus.ACCEPTED);
        verify(applicationRepository).save(application1);
    }

    @Test
    public void hireFailsTest() {
        given(applicationRepository.findAllByUserIdAndAndCourseId(userId1, courseId1)).willReturn(
                List.of(application2));
        applicationService.hire(userId1, courseId1);
        application1.setStatus(ApplicationStatus.ACCEPTED);
        verify(applicationRepository, never()).save(application1);
    }

    @Test
    public void getApplicationTest() {
        given(applicationRepository.findByUserIdAndCourseId(userId1, courseId1))
                .willReturn(Optional.of(application1));
        Optional<Application> actual = applicationService.getApplication(userId1, courseId1);
        assertTrue(actual.isPresent());
        assertEquals(application1, actual.get());
        verify(applicationRepository).findByUserIdAndCourseId(userId1, courseId1);
    }

    @Test
    public void getApplicationByIdTest() {
        given(applicationRepository.findById(application1.getApplicationId())).willReturn(
                Optional.of(application1));
        Optional<Application> actual = applicationService.getApplication(
                application1.getApplicationId());
        assertTrue(actual.isPresent());
        assertEquals(application1, actual.get());
        verify(applicationRepository).findById(application1.getApplicationId());
    }

    @Test
    public void withdrawApplicationTest() {
        Application applicationMock = Mockito.mock(Application.class);
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.of(applicationMock));

        applicationService.withdrawApplication(application1.getApplicationId());
        verify(applicationMock, times(1)).setStatus(ApplicationStatus.WITHDRAWN);
        verify(applicationRepository).save(applicationMock);
    }

    @Test
    public void rejectApplicationTest() {
        Application applicationMock = Mockito.mock(Application.class);
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.of(applicationMock));

        applicationService.rejectApplication(application1.getApplicationId());
        verify(applicationMock, times(1)).setStatus(ApplicationStatus.REJECTED);
        verify(applicationRepository).save(applicationMock);
    }

    @Test
    public void testSetMaxHoursPass() {
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.of(application1));

        applicationService.setMaxHours(application1.getApplicationId(), 150);
        application1.setMaxHours(150);

        verify(applicationRepository).save(application1);
    }

    @Test
    public void testSetMaxHoursFail() {
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                applicationService.setMaxHours(application1.getApplicationId(), 150));
    }

    @Test
    public void testGetMaxHoursPass() {
        when(applicationRepository.findAllByUserIdAndAndCourseId(application1.getUserId(),
                application1.getCourseId())).thenReturn(List.of(application1));

        assertEquals(applicationService.getMaxHours(application1.getUserId(),
                application1.getCourseId()), 200);
    }

    @Test
    public void testGetMaxHoursFail() {
        when(applicationRepository.findAllByUserIdAndAndCourseId(application1.getUserId(),
                application1.getCourseId())).thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () ->
                applicationService.getMaxHours(application1.getUserId(),
                        application1.getCourseId()));
    }

    @Test
    public void testSetRatingPass() {
        application1.setStatus(ApplicationStatus.ACCEPTED);
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.of(application1));

        applicationService.setRating(application1.getApplicationId(), 7.5);

        assertEquals(application1.getRating(), 7.5);
        verify(applicationRepository).save(application1);
    }

    @Test
    public void testSetRatingNotApproved() {
        application1.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.of(application1));

        assertThrows(IllegalStateException.class,
                () -> applicationService.setRating(application1.getApplicationId(), 7.5));
    }

    @Test
    public void testSetRatingNoApplication() {
        application1.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findById(application1.getApplicationId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> applicationService.setRating(application1.getApplicationId(), 7.5));
    }

    @Test
    public void testGetRatingPass() {
        application1.setStatus(ApplicationStatus.ACCEPTED);
        application1.setRating(7.5);
        when(applicationRepository
                .findAllByUserIdAndAndCourseId(application1.getUserId(),
                        application1.getCourseId()))
                .thenReturn(List.of(application1));

        assertEquals(applicationService
                        .getRating(application1.getUserId(), application1.getCourseId()),
                7.5);
    }

    @Test
    public void testGetRatingNotApproved() {
        application1.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository
                .findAllByUserIdAndAndCourseId(application1.getUserId(),
                        application1.getCourseId()))
                .thenReturn(List.of(application1));

        assertThrows(IllegalStateException.class, () -> applicationService
                        .getRating(application1.getUserId(), application1.getCourseId()));
    }

    @Test
    public void testGetRatingNotRated() {
        application1.setStatus(ApplicationStatus.ACCEPTED);
        when(applicationRepository
                .findAllByUserIdAndAndCourseId(application1.getUserId(),
                        application1.getCourseId()))
                .thenReturn(List.of(application1));

        assertThrows(IllegalStateException.class, () -> applicationService
                .getRating(application1.getUserId(), application1.getCourseId()));
    }

    @Test
    public void testGetRatingNoApplication() {
        when(applicationRepository
                .findAllByUserIdAndAndCourseId(application1.getUserId(),
                        application1.getCourseId()))
                .thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () -> applicationService
                .getRating(application1.getUserId(), application1.getCourseId()));
    }

}
