package nl.tudelft.sem.courses.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity(name = "Grades")
@Table(name = "grade")

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Grade {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @Column(name = "grade")
    private float gradeValue;



    @Column(name = "user_id")
    private long userId;


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


    /**
     * When using this constructor you must provide a course and a user.
     *
     * @param course Acceps a course object
     * @param userId accepts a user id
     */
    public Grade(Course course, long userId, float gradeValue) {
        this.course = course;
        this.userId = userId;
        this.gradeValue = gradeValue;
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
