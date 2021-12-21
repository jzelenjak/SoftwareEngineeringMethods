package nl.tudelft.sem.courses.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Entity(name = "Lecturer")
@Table(name = "lecturers")
@NoArgsConstructor
@Getter
@Setter
@IdClass(TeachesPk.class)
public class Teaches {
    @Id
    private long courseId;

    @Id
    private long lecturerId;

    /**
     * Teaches Constructor. Used for testing purposes.
     *
     * @param courseId - the Id of the course
     * @param lecturerId - the Id of the lecturer
     */
    public Teaches(long courseId, long lecturerId) {
        this.courseId = courseId;
        this.lecturerId = lecturerId;
    }

    /**
     * Equals method.
     *
     * @param o - the object we want to equate with
     * @return true if equal else it returns false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teaches teaches = (Teaches) o;
        return courseId == teaches.courseId && lecturerId == teaches.lecturerId;
    }

    /**
     * hashes the entity.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(courseId, lecturerId);
    }
}
