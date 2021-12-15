package nl.tudelft.sem.courses.communication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CourseRequest {

    private String courseCode;
    private LocalDate startDate;
    private LocalDate finishDate;

    /**
     * General course request constructor requires start date , finish date and the course id.
     *
     * @param courseCode -  The id of the course
     * @param startDate - The starting date of the course
     * @param finishDate - The finishing date of the course
     */
    public CourseRequest(String courseCode, LocalDate startDate, LocalDate finishDate) {
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

}
