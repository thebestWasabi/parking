package ru.gitverse.parking.model.dto;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public interface ParkingStatsDto {
    long getEnteredCount();
    long getExitedCount();
    Double getAvgParkingMinutes();
}
