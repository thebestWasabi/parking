package ru.gitverse.parking.exception;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public class ParkingConcurrentOperationException extends ParkingBusinessException {
    public ParkingConcurrentOperationException(String message) {
        super(message);
    }

    public ParkingConcurrentOperationException(final Throwable cause) {
        super(cause);
    }

    public ParkingConcurrentOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
