package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;

public class CourseRequest {

    public String courseId;
    public LocalDateTime startDate;

    public CourseRequest(String courseID, LocalDateTime startDate) {
        this.courseId = courseID;
        this.startDate = startDate;
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
}
