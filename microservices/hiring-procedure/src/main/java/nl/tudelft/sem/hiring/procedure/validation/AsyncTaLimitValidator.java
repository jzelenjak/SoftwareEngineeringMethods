package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.ApplicationStatus;
import nl.tudelft.sem.hiring.procedure.services.ApplicationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncTaLimitValidator extends AsyncBaseValidator {

    // There should be at most one TA per 20 students
    public static final int MIN_STUDENTS_PER_TA = 20;

    private final transient ApplicationService applicationService;

    private final transient CourseInfoResponseCache courseInfoCache;

    private final transient long courseId;

    /**
     * Constructor of the TA limit validator class.
     *
     * @param applicationService The application service.
     * @param courseId           Is the ID of the course.
     */
    public AsyncTaLimitValidator(ApplicationService applicationService,
                                 CourseInfoResponseCache courseInfoCache, long courseId) {
        this.applicationService = applicationService;
        this.courseInfoCache = courseInfoCache;
        this.courseId = courseId;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        return courseInfoCache.getCourseInfoResponse(courseId)
                .flatMap(responseBody -> {
                    var response = JsonParser.parseString(responseBody).getAsJsonObject();

                    // Retrieve the necessary data to perform the validation
                    long numberOfStudents = response.get("numberOfStudents").getAsLong();
                    long currentTaCount = applicationService.getApplicationsForCourse(courseId)
                            .stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                            .count();

                    // Check if the number of TAs does not exceed the limit for this course.
                    // Remaining/residual students can allow for one extra TA.
                    if (currentTaCount * MIN_STUDENTS_PER_TA
                            >= MIN_STUDENTS_PER_TA + numberOfStudents) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.METHOD_NOT_ALLOWED, "TA limit exceeded"));
                    }

                    // Continue with the request
                    return evaluateNext(headers, body);
                });
    }

}
