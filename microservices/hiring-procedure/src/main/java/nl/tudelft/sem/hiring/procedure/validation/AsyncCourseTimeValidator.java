package nl.tudelft.sem.hiring.procedure.validation;

import com.google.gson.JsonParser;
import java.time.Period;
import java.time.ZonedDateTime;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AsyncCourseTimeValidator extends AsyncBaseValidator {

    /// Duration used to validate course correctness (3 weeks in advance)
    public static final Period VALID_DURATION = Period.ofWeeks(3);

    // Course info cache
    private final transient CourseInfoResponseCache courseInfoCache;

    // Course ID
    private final transient long courseId;

    /**
     * Constructor of the course time validator class.
     *
     * @param courseId is the course ID.
     */
    public AsyncCourseTimeValidator(CourseInfoResponseCache courseInfoCache, long courseId) {
        this.courseInfoCache = courseInfoCache;
        this.courseId = courseId;
    }

    @Override
    public Mono<Boolean> validate(HttpHeaders headers, String body) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return courseInfoCache.getCourseInfoResponse(authorization, courseId)
                .flatMap(responseBody -> {
                    var response = JsonParser.parseString(responseBody).getAsJsonObject();
                    var start = ZonedDateTime.parse(response.get("startTime").getAsString());

                    // Check if the course registration period has not ended yet
                    if (ZonedDateTime.now().plus(VALID_DURATION).isAfter(start)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Course registration/withdrawal period has ended"));
                    }

                    // Continue with the request
                    return evaluateNext(headers, body);
                });
    }

}
