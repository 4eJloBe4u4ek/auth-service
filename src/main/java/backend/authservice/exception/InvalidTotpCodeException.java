package backend.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidTotpCodeException extends RuntimeException {
    public InvalidTotpCodeException(String message) {
        super(message);
    }
}
