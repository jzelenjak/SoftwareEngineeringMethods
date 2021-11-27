package nl.tudelft.sem.gateway.info;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

@AllArgsConstructor
@Data
public class EndpointInfo {

    // Endpoint/path to the targeted service
    @NotBlank(message = "A request must have a path")
    private String path;

    // Request method accepted by targeted service
    @NotNull
    private RequestMethod requestMethod;
}
