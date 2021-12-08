package nl.tudelft.sem.hiring.procedure.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {
    @Mock
    private transient ApplicationRepository applicationRepository;

    @Autowired
    @InjectMocks
    private transient ApplicationService applicationService;

    private transient Application application1;
    private transient Application application2;
    private transient Application application3;
    private transient Application application4;

    private transient LocalDateTime now;
    private transient LocalDateTime yearAgo;
    private transient LocalDateTime weekAgo;

    private final transient long userId1 = 521234;
    private final transient long courseId1 = 2450;
    private final transient long courseId2 = 2460;

    /**
     * Setup for each test.
     */
    @BeforeEach
    public void beforeEach() {
        now = LocalDateTime.now();
        yearAgo = now.minusYears(1).minusWeeks(1);
        weekAgo = now.minusWeeks(1);
        application1 = new Application(userId1, courseId1, now);
        application2 = new Application(userId1, courseId1, yearAgo);
        application3 = new Application(userId1, courseId2, now);
        application4 = new Application(userId1, courseId2, weekAgo);
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
}
