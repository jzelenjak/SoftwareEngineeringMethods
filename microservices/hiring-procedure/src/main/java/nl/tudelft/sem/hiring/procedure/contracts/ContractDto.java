package nl.tudelft.sem.hiring.procedure.contracts;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractDto {
    private String taName;
    private String courseCode;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private int maxHours = 200;

    /**
     * Full args constructor.
     *
     * @param taName the name of the TA
     * @param courseCode the code of the course
     * @param startDate the start date of the course in ZonedDateTime format
     * @param endDate the end date of the course in ZonedDateTime format
     * @param maxHours the maximum hours the TA is allowed to declare
     */
    public ContractDto(String taName, String courseCode,
                    ZonedDateTime startDate, ZonedDateTime endDate, int maxHours) {
        this.taName = taName;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxHours = maxHours;
    }

    /**
     * Zero args constructor.
     */
    public ContractDto() {
    }
}
