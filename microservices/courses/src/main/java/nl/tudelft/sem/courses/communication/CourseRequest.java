package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CourseRequest {

    private String courseCode;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    /**
     * General course request constructor requires start date , finish date and the course id.
     *
     * @param courseCode -  The id of the course
     * @param startDate - The starting date of the course
     * @param finishDate - The finishing date of the course
     */
    public CourseRequest(String courseCode, LocalDateTime startDate, LocalDateTime finishDate) {
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }
}
