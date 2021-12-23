package nl.tudelft.sem.courses.entities;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
     * Teaches Constructor. Used in testing to reduce the number
     * of lines in the code caused by setter methods.
     *
     * @param courseId - the Id of the course
     * @param lecturerId - the Id of the lecturer
     */
    public Teaches(long courseId, long lecturerId) {
        this.courseId = courseId;
        this.lecturerId = lecturerId;
    }

    /**
     * Checks if an object is equal to the Teaches object.
     *
     * @param o - the object we want to equate with
     * @return true if equal else it returns false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Teaches teaches = (Teaches) o;
        return courseId == teaches.courseId && lecturerId == teaches.lecturerId;
    }

    /**
     * hashes the entity based on the
     * course id and the lecturer id.
     * Is required for storing in repository.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(courseId, lecturerId);
    }
}
