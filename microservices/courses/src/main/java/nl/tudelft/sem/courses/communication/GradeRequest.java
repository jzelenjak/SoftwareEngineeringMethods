package nl.tudelft.sem.courses.communication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeRequest {

    private long courseId;
    private float grade;
    private long userId;

    /**
     * A JSON serializable request object for adding a new grade.
     *
     *
     * @param courseId - id of the course
     * @param grade - the grade for the course
     * @param userId - the users identification number
     */
    public GradeRequest(long courseId, float grade, long userId) {
        this.courseId = courseId;
        this.grade = grade;
        this.userId = userId;
    }

}
