package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncTaLimitValidator extends AsyncBaseValidator {

    // There should be at most one TA per 20 students
    public static final int MIN_STUDENTS_PER_TA = 20;

    private final transient SubmissionService submissionService;

    private final transient CourseInfoResponseCache courseInfoCache;

    private final transient long courseId;

    /**
     * Constructor of the TA limit validator class.
     *
     * @param submissionService The application service.
     * @param courseId           Is the ID of the course.
     */
    public AsyncTaLimitValidator(SubmissionService submissionService,
                                 CourseInfoResponseCache courseInfoCache, long courseId) {
        this.submissionService = submissionService;
        this.courseInfoCache = courseInfoCache;
        this.courseId = courseId;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        return courseInfoCache.getCourseInfoResponse(headers.getFirst(HttpHeaders.AUTHORIZATION),
                        courseId)
                .flatMap(responseBody -> {
                    var response = JsonParser.parseString(responseBody).getAsJsonObject();

                    // Retrieve the necessary data to perform the validation
                    long numberOfStudents = response.get("numberOfStudents").getAsLong();
                    long currentTaCount = submissionService.getSubmissionsForCourse(courseId)
                            .stream()
                            .filter(app -> app.getStatus() == SubmissionStatus.ACCEPTED)
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
