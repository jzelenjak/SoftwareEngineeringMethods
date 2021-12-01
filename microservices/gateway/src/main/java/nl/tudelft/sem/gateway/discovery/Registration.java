package nl.tudelft.sem.gateway.discovery;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Registration {

    // Hostname of the registered microservice
    @NotBlank(message = "Host address of microservice cannot be blank.")
    private String host;

    // Port of the registered microservice
    private int port;

    /**
     * Returns the remote address by appending the port to the host name.
     *
     * @return remote address.
     */
    public String remoteAddress() {
        return host + ':' + port;
    }
}
