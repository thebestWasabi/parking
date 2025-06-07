package ru.gitverse.parking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Getter
public class ParkingException extends RuntimeException {
    private final HttpStatus status;

    public ParkingException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }
}
