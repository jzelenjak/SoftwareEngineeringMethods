package nl.tudelft.sem.courses.communication;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseResponse {

    private long courseId;
    private String courseCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long numberOfStudents;

    /**
     * A response object containing the details of a course.
     * Course objects can contain too much data to send across,
     *  thus the need for a new object structure.
     *
     *
     * @param courseId - The courses identification
     * @param courseCode - The courses course code
     * @param startDate - The start date of the course
     * @param endDate - The end date of the course
     * @param numberOfStudents - The number of students in the course.
     */
    public CourseResponse(long courseId, String courseCode, LocalDateTime startDate, LocalDateTime endDate, long numberOfStudents) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfStudents = numberOfStudents;
    }

}
