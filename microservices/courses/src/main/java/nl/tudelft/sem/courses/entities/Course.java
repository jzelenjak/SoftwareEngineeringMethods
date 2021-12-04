package nl.tudelft.sem.courses.entities;


import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Courses")
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;


    @Column(name = "courseID")
    public String courseID;


    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "grades")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Grade> grades;

    /**
     *  Empty constructor for Jpa Persistance.
     */
    public Course(){}


    /**
     * Constructor for Course. You must specify a course ID when creating the course.
     * This course ID can be changed later.
     * @param courseID
     */
    public Course(long id, String courseID, LocalDateTime startDate){
        this.id = id;
        this.courseID = courseID;
        grades = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseID.equals(course.courseID) && id == course.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID, grades , id);
    }


    @Override
    public String toString() {
        return "Course{" +
                "courseID='" + courseID + '\'' +
                ", users=" + grades.toString() +
                '}';
    }
}
