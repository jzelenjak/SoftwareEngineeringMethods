package nl.tudelft.sem.gateway.discovery;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Registration {

    @NotBlank(message = "Path to a microservice cannot be blank.")
    private String path;
}
