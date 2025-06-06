package ru.gitverse.parking.exception;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public class ParkingBusinessException extends RuntimeException {
    public ParkingBusinessException(String message) {
        super(message);
    }

    public ParkingBusinessException(final Throwable cause) {
        super(cause);
    }

    public ParkingBusinessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
