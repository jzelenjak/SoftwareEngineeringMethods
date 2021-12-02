package nl.tudelft.sem.courses.entities;


import lombok.NoArgsConstructor;

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
    public float grade;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grades")
    public User user;

    /**
     *  Empty constructor for Jpa Persistance.
     */
    public Grade(){

    }

    /**
     * When using this constructor you must provide a course and a user.
     * @param course
     * @param user
     */
    public Grade(long id, Course course, User user, float grade){
        this.id = id;
        this.course = course;
        this.user = user;
        this.grade = grade;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade1 = (Grade) o;
        return Float.compare(grade1.grade, grade) == 0 && course.equals(grade1.course) && user.equals(grade1.user) || id == grade1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, grade, user);
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id = " + id +
                ", course=" + course +
                ", user=" + user +
                '}';
    }
}
