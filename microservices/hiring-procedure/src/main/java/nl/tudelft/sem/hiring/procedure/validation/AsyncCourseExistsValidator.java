package nl.tudelft.sem.hiring.procedure.validation;

import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * Validator class for checking with the Course microservice if a given course exists.
 * Used for methods that don't take the start time of the course in consideration.
 */
public class AsyncCourseExistsValidator extends AsyncBaseValidator {

    // Cache for course info
    private final transient CourseInfoResponseCache courseInfoCache;

    // Course ID
    private final transient long courseId;

    /**
     * Constructor of the course exists validator class.
     *
     * @param courseInfoCache Cache for course info.
     * @param courseId        is the course ID.
     */
    public AsyncCourseExistsValidator(CourseInfoResponseCache courseInfoCache, long courseId) {
        this.courseInfoCache = courseInfoCache;
        this.courseId = courseId;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        return courseInfoCache.getCourseInfoResponse(headers.getFirst(HttpHeaders.AUTHORIZATION),
                        courseId)
                .flatMap(response -> evaluateNext(headers, body));
    }
}
