package ru.gitverse.parking.model.enums;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public enum ParkingEventType {
    ENTRY,
    EXIT;

    public static ParkingEventType fromString(final String str) {
        for (ParkingEventType type : ParkingEventType.values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid parking event type: " + str);
    }
}
