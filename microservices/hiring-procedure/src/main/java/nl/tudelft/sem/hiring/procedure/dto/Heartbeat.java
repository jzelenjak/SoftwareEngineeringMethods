package nl.tudelft.sem.hiring.procedure.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Heartbeat {
    @NotBlank(message = "Path to a microservice cannot be blank.")
    private final String path;
}

