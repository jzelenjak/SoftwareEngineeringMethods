package nl.tudelft.sem.courses.communication;

import java.time.LocalDateTime;

public class CourseRequest {

    public String courseID;
    public LocalDateTime startDate;

    public CourseRequest(String courseID, LocalDateTime startDate){
        this.courseID = courseID;
        this.startDate = startDate;
    }


}
