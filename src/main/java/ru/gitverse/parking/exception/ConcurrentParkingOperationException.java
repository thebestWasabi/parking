package ru.gitverse.parking.exception;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public class ConcurrentParkingOperationException extends RuntimeException {
    public ConcurrentParkingOperationException(String message) {
        super(message);
    }

    public ConcurrentParkingOperationException(final Throwable cause) {
        super(cause);
    }

    public ConcurrentParkingOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
