package ru.gitverse.parking.service;

import org.springframework.stereotype.Repository;
import ru.gitverse.parking.dto.ParkingEntryRequest;
import ru.gitverse.parking.dto.ParkingExitRequest;
import ru.gitverse.parking.dto.ParkingReport;
import ru.gitverse.parking.dto.ParkingResponse;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Repository
public interface ParkingService {
    ParkingResponse entry(ParkingEntryRequest request);

    ParkingResponse exit(ParkingExitRequest request);

    ParkingReport generateReport(LocalDateTime start, LocalDateTime end);
}
