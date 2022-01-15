package nl.tudelft.sem.courses.communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentGradeTuple {
    private Long studentId;

    private Float grade;
}
