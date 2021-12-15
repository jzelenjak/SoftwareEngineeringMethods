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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity(name = "Courses")
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "course_id")
    private long id;


    @Column(name = "course_code")
    public String courseCode;


    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "grades")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Grade> grades;

    @Column(name = "finish_date")
    public LocalDateTime finishDate;



    /**
     * Constructor for Course. The course ID will be automatically
     * generated when the entity is created.
     * This course ID can be changed later.
     *
     * @param courseCode - Course ID in a string format
     */
    public Course(String courseCode, LocalDateTime startDate, LocalDateTime finishDate) {
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        grades = new HashSet<>();
    }



    /**
     * Constructor for Course. You must specify a course ID
     * when creating the course in this constructor.
     * This course ID can be changed later. Mainly used for testing purposes.
     *
     * @param courseCode - Course ID in a string format
     */
    public Course(long id, String courseCode, LocalDateTime startDate, LocalDateTime finishDate) {
        this.id = id;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        grades = new HashSet<>();
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
        return id == course.id || courseCode.equals(course.courseCode)
                && startDate.equals(course.startDate)
                && finishDate.equals(course.finishDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode, grades, id);
    }


    @Override
    public String toString() {
        return "Course{"
                + "course code='" + courseCode + '\''
                + ", users=" + grades.toString()
                + '}';
    }
}
