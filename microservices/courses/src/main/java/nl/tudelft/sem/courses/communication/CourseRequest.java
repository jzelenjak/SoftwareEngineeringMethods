package nl.tudelft.sem.courses.communication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CourseRequest {

    private String courseCode;
    private ZonedDateTime startDate;
    private ZonedDateTime finishDate;
    private int numStudents;

    /**
     * General course request constructor requires start date , finish date and the course id.
     *
     * @param courseCode -  The id of the course
     * @param startDate - The starting date of the course
     * @param finishDate - The finishing date of the course
     */
    public CourseRequest(String courseCode, ZonedDateTime startDate,
                         ZonedDateTime finishDate, int numStudents) {
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.numStudents = numStudents;
    }

}
