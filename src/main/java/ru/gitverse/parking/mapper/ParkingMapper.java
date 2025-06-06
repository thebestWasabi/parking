package ru.gitverse.parking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gitverse.parking.dto.ParkingEntryRequest;
import ru.gitverse.parking.dto.ParkingResponse;
import ru.gitverse.parking.entity.ParkingEventType;
import ru.gitverse.parking.entity.ParkingLog;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Component
public final class ParkingMapper {
    public ParkingLog toEntity(final ParkingEntryRequest request) {
        final ParkingLog log = new ParkingLog();
        log.setCarNumber(request.getCarNumber());
        log.setCarType(request.getCarType());
        log.setEventType(ParkingEventType.ENTRY);
        log.setEventTime(LocalDateTime.now());
        log.setEventChainStart(log.getEventTime());
        return log;
    }

    public ParkingResponse toResponse(final ParkingLog parkingLog) {
        final ParkingResponse response = new ParkingResponse();
        response.setDateTime(parkingLog.getEventTime());
        return response;
    }

    public ParkingLog toExitEvent(final ParkingLog parkingLog) {
        final ParkingLog exitLog = new ParkingLog();
        exitLog.setCarNumber(parkingLog.getCarNumber());
        exitLog.setCarType(parkingLog.getCarType());
        exitLog.setEventType(ParkingEventType.EXIT);
        exitLog.setEventTime(LocalDateTime.now());
        exitLog.setEventChainStart(exitLog.getEventChainStart());
        return exitLog;
    }
}
