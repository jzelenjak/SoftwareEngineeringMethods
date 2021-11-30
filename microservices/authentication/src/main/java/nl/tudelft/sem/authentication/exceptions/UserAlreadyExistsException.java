package nl.tudelft.sem.authentication.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_GATEWAY, reason = "Username already taken")
public class UserAlreadyExistsException extends Exception {
    private static final long serialVersionUID = 87654546255446278L;
}
