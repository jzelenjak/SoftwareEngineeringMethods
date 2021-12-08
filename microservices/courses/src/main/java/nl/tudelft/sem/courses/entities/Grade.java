package nl.tudelft.sem.courses.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity(name = "Grades")
@Table(name = "grade")

@Getter @Setter @NoArgsConstructor
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Grade grade1 = (Grade) o;
        return Float.compare(grade1.gradeValue, gradeValue) == 0
                && course.equals(grade1.course) && userId == grade1.userId
                || id == grade1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, gradeValue, userId);
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
