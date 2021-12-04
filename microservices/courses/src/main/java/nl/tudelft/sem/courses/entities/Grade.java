package nl.tudelft.sem.courses.entities;


import javax.persistence.*;
import java.util.Objects;

@Entity(name = "Grades")
@Table(name = "grade")
public class Grade {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;


    @ManyToOne(fetch = FetchType.LAZY)
    public Course course;

    @Column(name = "grade")
    public float gradeValue;



    @Column(name = "userID")
    public long userID;

    /**
     *  Empty constructor for Jpa Persistance.
     */
    public Grade(){

    }

    /**
     * When using this constructor you must provide a course and a user.
     * @param course
     * @param userID
     */
    public Grade(long id, Course course, long userID, float gradeValue){
        this.id = id;
        this.course = course;
        this.userID = userID;
        this.gradeValue = gradeValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public float getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(float gradeValue) {
        this.gradeValue = gradeValue;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade1 = (Grade) o;
        return Float.compare(grade1.gradeValue, gradeValue) == 0 && course.equals(grade1.course) && userID == grade1.userID || id == grade1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, gradeValue, userID);
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id = " + id +
                ", course=" + course +
                ", user=" + userID +
                '}';
    }
}
