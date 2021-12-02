package nl.tudelft.sem.courses.entities;

import javax.annotation.processing.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Users")
@Table(name = "users")
public class User {

    /**
     * You must provide an Identification for the user. The courses are added later.
     * @param id
     * @param firstname
     * @param lastName
     * @param role
     */
    public User(long id, String firstname, String lastName, Role role){
        this.id = id;
        this.firstName = firstname;
        this.lastName = lastName;
        this.role = role;
        this.courses = new HashSet<>();
    }

    /**
     * Alternative constructor where you can just provide the courses in addition to the other parameters.
     * @param id
     * @param firstname
     * @param lastName
     * @param role
     * @param courses
     */
    public User(long id, String firstname, String lastName, Role role, Set<Course> courses){
        this.id = id;
        this.firstName = firstname;
        this.lastName = lastName;
        this.role = role;
        if(courses != null){
            this.courses = courses;
        }else {
            this.courses = new HashSet<>();
        }
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public long id;

    @Column(name = "first name")
    public String firstName;

    @Column(name = "last name")
    public String lastName;

    @Column(name = "student number")
    public int studentNumber;

    @Column(name = "role")
    public Role role;

    @ManyToMany(mappedBy = "course")
    @Column(name = "courses")
    public Set<Course> courses;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(int studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && studentNumber == user.studentNumber && firstName.equals(user.firstName) && lastName.equals(user.lastName) && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, studentNumber, role);
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", studentNumber=" + studentNumber +
                ", role=" + role +
                ", courses=" + courses +
                '}';
    }
}
