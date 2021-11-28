package nl.tudelft.sem.gateway.info;

import lombok.Data;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;

@Data
public class EndpointInfo {

    // Endpoint/path to the targeted service
    private String path;

    // Request method accepted by targeted service
    private HttpMethod method;

    /**
     * Initialize an EndpointInfo object and do validation on it.
     * @param path URI path of the request
     * @param method request of method (GET, POST, PUT, DELETE)
     */
    public EndpointInfo(String path, HttpMethod method) throws IllegalArgumentException{
        if (path == null || path.isBlank()) throw new IllegalArgumentException("A request must have a path");
        if (method == null) throw new IllegalArgumentException("A request must have a method");

        this.path = path;
        this.method = method;
    }

}
