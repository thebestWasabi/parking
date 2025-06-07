package ru.gitverse.parking.service;

import org.springframework.stereotype.Repository;
import ru.gitverse.parking.model.dto.ParkingEntryRequest;
import ru.gitverse.parking.model.dto.ParkingExitRequest;
import ru.gitverse.parking.model.dto.ParkingReport;
import ru.gitverse.parking.model.dto.ParkingResponse;
import ru.gitverse.parking.model.dto.ReportDto;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Repository
public interface ParkingService {
    ParkingResponse entry(ParkingEntryRequest request);

    ParkingResponse exit(ParkingExitRequest request);

    ParkingReport generateReport(ReportDto reportDto);
}
