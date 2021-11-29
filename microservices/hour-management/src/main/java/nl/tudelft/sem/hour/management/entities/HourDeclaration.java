package nl.tudelft.sem.hour.management.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class HourDeclaration {

    @Id
    @Column(name = "declaration_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long declarationId;

    @Column(name = "student_id")
    private long studentId;

    @Column(name = "course_id")
    private long courseId;

    @Column(name = "approved")
    private boolean approved;

    @Column(name = "declared_hours")
    private double declaredHours;

    @Column(name = "declaration_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime declarationDate;

    /**
     * Constructs an HourDeclaration DOA instance.
     *
     * @param studentId      is the ID of the student.
     * @param courseId       is the ID of the course.
     * @param hours_declared is the amount of hours that the student declared.
     * @implNote All new entries are by default marked as unapproved.
     */
    public HourDeclaration(long studentId, long courseId, double declaredHours) {
        this.declarationId = 0;
        this.studentId = studentId;
        this.courseId = courseId;
        this.declaredHours = declaredHours;

        this.approved = false;
        this.declarationDate = LocalDateTime.now();
    }

    /**
     * Constructs an HourDeclaration DOA instance.
     *
     * @param declarationId  is the ID of the declaration.
     * @param studentId      is the ID of the student.
     * @param courseId       is the ID of the course.
     * @param hours_declared is the amount of hours that the student declared.
     * @implNote All new entries are by default marked as unapproved.
     */
    public HourDeclaration(long declarationId, long studentId, long courseId, double declaredHours) {
        this.declarationId = declarationId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.declaredHours = declaredHours;

        this.approved = false;
        this.declarationDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HourDeclaration that = (HourDeclaration) o;
        return getDeclarationId() == that.getDeclarationId()
                && getStudentId() == that.getStudentId()
                && getCourseId() == that.getCourseId()
                && Double.compare(that.getDeclaredHours(), getDeclaredHours()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclarationId(), getStudentId(), getCourseId(), getDeclaredHours());
    }

    @Override
    public String toString() {
        return "HourDeclaration{" +
                "declarationId=" + declarationId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", approved=" + approved +
                ", declaredHours=" + declaredHours +
                ", declarationDate=" + declarationDate +
                '}';
    }
}

