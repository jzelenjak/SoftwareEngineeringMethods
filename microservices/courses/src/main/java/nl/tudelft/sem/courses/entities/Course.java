package nl.tudelft.sem.courses.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
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
    @ManyToMany
    private Set<User> users;


    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "grades")
    @OneToMany(mappedBy = "course")
    private Set<Grade> grades;

    /**
     * Constructor for Course. You must specify a course ID when creating the course.
     * This course ID can be changed later.
     * @param courseID
     */
    public Course(String courseID){
        this.courseID = courseID;
        users = new HashSet<>();
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public Set<User> getUser() {
        return users;
    }

    public void setUser(Set<User> user) {
        this.users = user;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
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
        return courseID.equals(course.courseID) && users.equals(course.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID, users);
    }


    @Override
    public String toString() {
        return "Course{" +
                "courseID='" + courseID + '\'' +
                ", users=" + users.toString() +
                '}';
    }
}
