package nl.tudelft.sem.courses.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Courses")
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "courseID")
    public String courseID;


    @Column(name = "users")
    @ManyToMany(mappedBy = "user")
    private Set<User> user;

    /**
     * Constructor for Course. You must specify a course ID when creating the course.
     * This course ID can be changed later.
     * @param courseID
     */
    public Course(String courseID){
        this.courseID = courseID;
        user = new HashSet<>();
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public Set<User> getUser() {
        return user;
    }

    public void setUser(Set<User> user) {
        this.user = user;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseID.equals(course.courseID) && user.equals(course.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID, user);
    }


    @Override
    public String toString() {
        return "Course{" +
                "courseID='" + courseID + '\'' +
                ", user=" + user +
                '}';
    }
}
