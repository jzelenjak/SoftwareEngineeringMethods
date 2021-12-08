package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;

public class CourseRequest {

    private String courseId;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

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
