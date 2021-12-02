package nl.tudelft.sem.courses.entities;

import javax.annotation.processing.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
     * @param role
     */
    public User(long id, Role role){
        this.id = id;
        this.role = role;
        this.courses = new HashSet<>();
    }

    /**
     * Alternative constructor where you can just provide the courses in addition to the other parameters.
     * @param id
     * @param role
     * @param courses
     */
    public User(long id, Role role, Set<Course> courses){
        this.id = id;

        this.role = role;
        if(courses != null){
            this.courses = courses;
        }else {
            this.courses = new HashSet<>();
        }
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public long id;

    @Column(name = "role")
    public Role role;

    @ManyToMany
    @Column(name = "courses")
    public Set<Course> courses;


    @OneToMany(mappedBy = "user")
    @Column(name = "grades")
    public Set<Grade> grades;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return id == user.id  && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role);
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", role=" + role +
                ", courses=" + courses +
                '}';
    }
}
