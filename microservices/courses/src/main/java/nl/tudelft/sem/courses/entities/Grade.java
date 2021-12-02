package nl.tudelft.sem.courses.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = 'Grades')
@Table(name = 'grade')
public class Grade {



    @Id
    @OneToOne(name = 'course')
    @Column(name = 'course')
    public Course course;

    @Id
    @OneToOne(name = 'user')
    @Column(name = 'user')
    public User user;

    /**
     * When using this constructor you must provide a course and a user.
     * @param course
     * @param user
     */
    public Grade(Course course, User user){
        this.course = course;
        this.user = user;
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
        Grade grade = (Grade) o;
        return course.equals(grade.course) && user.equals(grade.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, user);
    }


    @Override
    public String toString() {
        return "Grade{" +
                "course=" + course +
                ", user=" + user +
                '}';
    }
}
