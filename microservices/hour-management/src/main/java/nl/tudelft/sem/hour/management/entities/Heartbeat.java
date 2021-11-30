package nl.tudelft.sem.hour.management.entities;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Heartbeat {
    @NotBlank(message = "Path to a microservice cannot be blank.")
    private final String path;
}
