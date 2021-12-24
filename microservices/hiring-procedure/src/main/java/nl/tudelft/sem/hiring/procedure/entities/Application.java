package nl.tudelft.sem.hiring.procedure.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Application {

    @Id
    @Column(name = "application_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long applicationId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "course_id")
    private long courseId;

    /**
     * Specifies the status of the application.
     */
    @Column(name = "status")
    private ApplicationStatus status;

    @Column(name = "max_hours")
    private int maxHours = 200;

    @Column(name = "rating")
    private double rating = -1.0;

    @Column(name = "submission_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime submissionDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    /**
     * Constructor used generally to declare an Application.
     *
     * @param userId The userId of the user that sent the request
     * @param courseId The courseId of the course the user wishes to apply to
     * @param submissionDate The date at which the microservice received the request
     */
    public Application(long userId, long courseId, LocalDateTime submissionDate) {
        this.userId = userId;
        this.courseId = courseId;
        this.status = ApplicationStatus.IN_PROGRESS;
        this.submissionDate = submissionDate;
    }

    /**
     * Constructor used for testing (production!). Allows to specify the applicationId.
     *
     * @param applicationId Specified id for testing purposes
     * @param userId The userId of the user that sent the request
     * @param courseId The courseId of the course the user wishes to apply to
     * @param submissionDate The date at which the microservice received the request
     */
    public Application(long applicationId, long userId, long courseId,
                       LocalDateTime submissionDate) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.courseId = courseId;
        this.status = ApplicationStatus.IN_PROGRESS;
        this.submissionDate = submissionDate;
    }
}