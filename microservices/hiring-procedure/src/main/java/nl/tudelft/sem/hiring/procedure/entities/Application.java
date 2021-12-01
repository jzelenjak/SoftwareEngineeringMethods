package nl.tudelft.sem.hiring.procedure.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
public class Application {

    @Id
    @Column(name = "application_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long applicationId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "course_id")
    private long courseId;

    @Column(name = "status")
    private int status;

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
     */
    public Application(long userId, long courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.status = 0;
        this.submissionDate = LocalDateTime.now();
    }

    /**
     * Constructor used for testing (production!). Allows to specify the applicationId.
     *
     * @param applicationId Specified id for testing purposes
     * @param userId The userId of the user that sent the request
     * @param courseId The courseId of the course the user wishes to apply to
     */
    public Application(long applicationId, long userId, long courseId) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.courseId = courseId;
        this.status = 0;
        this.submissionDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Application that = (Application) o;
        return applicationId == that.applicationId && userId == that.userId
            && courseId == that.courseId && status == that.status
            && Objects.equals(submissionDate, that.submissionDate)
            && Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, userId, courseId, status, submissionDate, lastUpdate);
    }

    @Override
    public String toString() {
        return "Application{"
            + "applicationId=" + applicationId
            + ", userId=" + userId
            + ", courseId=" + courseId
            + ", status=" + status
            + ", submissionDate=" + submissionDate
            + ", lastUpdate=" + lastUpdate
            + '}';
    }
}
