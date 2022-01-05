package nl.tudelft.sem.courses.entities;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeachesPk implements Serializable {
    private Long courseId;
    private Long lecturerId;
    public static final long serialVersionUID = 1;
}
