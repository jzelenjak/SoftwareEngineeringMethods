package nl.tudelft.sem.hour.management.entities;

import java.time.ZonedDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
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

    @Column(name = "description")
    private String description;

    @Column(name = "declaration_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime declarationDate;

    /**
     * Construct an HourDeclaration DOA instance.
     *
     * @param hourDeclarationRequest DTO of creating a new HourDeclaration
     */
    public HourDeclaration(HourDeclarationRequest hourDeclarationRequest) {
        this.declarationId = 0;
        this.studentId = hourDeclarationRequest.getStudentId();
        this.courseId = hourDeclarationRequest.getCourseId();
        this.declaredHours = hourDeclarationRequest.getDeclaredHours();
        this.description = hourDeclarationRequest.getDescription();

        this.approved = false;
        this.declarationDate = ZonedDateTime.now();
    }

    /**
     * Construct an HourDeclaration DOA instance for testing purposes.
     *
     * @param declarationId id of declaration
     * @param hourDeclarationRequest DTO of creating a new HourDeclaration
     * @param approved whether declaration is approved or not
     * @param declarationDate date of declaration
     */
    public HourDeclaration(long declarationId, HourDeclarationRequest hourDeclarationRequest,
                           boolean approved, ZonedDateTime declarationDate) {
        this.declarationId = declarationId;
        this.studentId = hourDeclarationRequest.getStudentId();
        this.courseId = hourDeclarationRequest.getCourseId();
        this.declaredHours = hourDeclarationRequest.getDeclaredHours();
        this.description = hourDeclarationRequest.getDescription();

        this.approved = approved;
        this.declarationDate = declarationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HourDeclaration that = (HourDeclaration) o;
        return getDeclarationId() == that.getDeclarationId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclarationId());
    }
}

