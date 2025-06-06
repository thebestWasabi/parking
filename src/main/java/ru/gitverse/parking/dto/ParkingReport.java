package ru.gitverse.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingReport {
    private int enteredCount;
    private int exitedCount;
    private double avgParkingMinutes;
}
