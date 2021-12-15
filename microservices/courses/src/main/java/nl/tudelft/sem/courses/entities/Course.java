package nl.tudelft.sem.courses.entities;





import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    private ZonedDateTime startDate;

    @Column(name = "grades")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Grade> grades;

    @Column(name = "finish_date")
    public ZonedDateTime finishDate;

    @Column(name = "number_of_students")
    public int numStudents;



    /**
     * Constructor for Course. The course ID will be automatically
     * generated when the entity is created.
     * This course ID can be changed later.
     *
     * @param courseCode - Course ID in a string format
     */
    public Course(String courseCode, ZonedDateTime startDate,
                  ZonedDateTime finishDate, int numStudents) {
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.numStudents = numStudents;
        grades = new HashSet<>();
    }



    /**
     * Constructor for Course. You must specify a course ID
     * when creating the course in this constructor.
     * This course ID can be changed later. Mainly used for testing purposes.
     *
     * @param courseCode - Course ID in a string format
     */
    public Course(long id, String courseCode, ZonedDateTime startDate,
                  ZonedDateTime finishDate, int numStudents) {
        this.id = id;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.numStudents = numStudents;
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
                && datesEqual(startDate, course.startDate)
                && datesEqual(finishDate, course.finishDate)
                && numStudents == course.numStudents;
    }

    /**
     * Custom equals method for two ZonedDateTime.
     * Only compares year, month and day.
     *
     * @param date1 - The first date
     * @param date2 - The secon date
     * @return - true if equals else false.
     */
    public boolean datesEqual(ZonedDateTime date1, ZonedDateTime date2) {
        return date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth()
                && date1.getDayOfMonth() == date2.getDayOfMonth();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Course{"
                + "course code='" + courseCode + '\''
                + ", users=" + grades.toString()
                + '}';
    }
}
