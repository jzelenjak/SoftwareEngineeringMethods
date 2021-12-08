package nl.tudelft.sem.courses.entities;



import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity(name = "Courses")
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;


    @Column(name = "courseId")
    public String courseId;


    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "grades")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Grade> grades;

    @Column(name = "finish_date")
    public LocalDateTime finishDate;

    /**
     *  Empty constructor for Jpa Persistence.
     */
    public Course() {}


    /**
     * Constructor for Course. You must specify a course ID when creating the course.
     * This course ID can be changed later.
     *
     * @param courseId - Course ID in a string format
     */
    public Course(long id, String courseId, LocalDateTime startDate, LocalDateTime finishDate) {
        this.id = id;
        this.courseId = courseId;
        this.startDate = startDate;
        this.finishDate = finishDate;
        grades = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Course course = (Course) o;
        return courseId.equals(course.courseId) && id == course.id && startDate.equals(course.startDate) && finishDate.equals(course.finishDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, grades, id);
    }


    @Override
    public String toString() {
        return "Course{"
                + "courseID='" + courseId + '\''
                + ", users=" + grades.toString()
                + '}';
    }
}
