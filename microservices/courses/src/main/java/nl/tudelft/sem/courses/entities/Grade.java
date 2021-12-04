package nl.tudelft.sem.courses.entities;


import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


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



    @Column(name = "userId")
    public long userId;

    /**
     *  Empty constructor for Jpa Persistance.
     */
    public Grade(){

    }

    /**
     * When using this constructor you must provide a course and a user.
     *
     * @param course Acceps a course object
     * @param userId accepts a user id
     */
    public Grade(long id, Course course, long userId, float gradeValue) {
        this.id = id;
        this.course = course;
        this.userId = userId;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Grade grade1 = (Grade) o;
        return Float.compare(grade1.gradeValue, gradeValue) == 0
                && course.equals(grade1.course) && userId == grade1.userId
                || id == grade1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, gradeValue, userId);
    }

    @Override
    public String toString() {
        return "Grade{"
                + "id = " + id
                + ", course=" + course
                + ", user=" + userId
                + '}';
    }
}
