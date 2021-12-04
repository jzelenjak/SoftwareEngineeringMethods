package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;

public class CourseRequest {

    public String courseID;
    public LocalDateTime startDate;

    public CourseRequest(String courseID, LocalDateTime startDate){
        this.courseID = courseID;
        this.startDate = startDate;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
}
