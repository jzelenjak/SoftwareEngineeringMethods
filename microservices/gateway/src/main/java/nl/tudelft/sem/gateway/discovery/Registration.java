package nl.tudelft.sem.gateway.discovery;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Registration {

    @NotBlank(message = "Path to a microservice cannot be blank.")
    private String path;
}
