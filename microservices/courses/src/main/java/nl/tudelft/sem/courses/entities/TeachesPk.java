package nl.tudelft.sem.courses.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeachesPk implements Serializable {
    private Long courseId;
    private Long lecturerId;
    public static final long serialVersionUID = 1;
}
