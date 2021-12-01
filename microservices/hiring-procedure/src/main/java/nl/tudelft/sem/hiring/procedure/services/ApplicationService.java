package nl.tudelft.sem.hiring.procedure.services;

import java.time.LocalDateTime;
import java.util.List;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public boolean checkSameApplication(long userId, long courseId) {
        int currentYear = LocalDateTime.now().getYear();
        List<Application> applications = applicationRepository
            .findAllByUserIdAndAndCourseId(userId, courseId);

        if (applications.size() == 0)
            return true;
        for (Application application: applications)
            if (application.getSubmissionDate().getYear() == currentYear)
                return false;

        return true;
    }

    public boolean checkDeadline(LocalDateTime courseStart) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime deadline = courseStart.minusWeeks(3);
        return currentTime.isBefore(deadline);
    }

    public void createApplication(long userId, long courseId) {
        LocalDateTime currentTime = LocalDateTime.now();
        Application application = new Application(userId, courseId, currentTime);
        applicationRepository.save(application);
    }
}
