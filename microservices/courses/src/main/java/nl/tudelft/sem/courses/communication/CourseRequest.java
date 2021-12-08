package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CourseRequest {

    private String courseId;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    /**
     * General course request constructor requires start date , finish date and the course id.
     *
     * @param courseId -  The id of the course
     * @param startDate - The starting date of the course
     * @param finishDate - The finishing date of the course
     */
    public CourseRequest(String courseId, LocalDateTime startDate, LocalDateTime finishDate) {
        this.courseId = courseId;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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
