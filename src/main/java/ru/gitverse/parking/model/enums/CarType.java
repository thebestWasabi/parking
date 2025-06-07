package ru.gitverse.parking.model.enums;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public enum CarType {
    PASSENGER,
    TRUCK;

    public static CarType fromString(final String str) {
        for (CarType carType : CarType.values()) {
            if (carType.name().equalsIgnoreCase(str)) {
                return carType;
            }
        }
        throw new IllegalArgumentException("Invalid car type: " + str);
    }
}
