package ru.gitverse.parking.mapper;

import org.springframework.stereotype.Component;
import ru.gitverse.parking.model.dto.ParkingEntryRequest;
import ru.gitverse.parking.model.dto.ParkingResponse;
import ru.gitverse.parking.model.enums.ParkingEventType;
import ru.gitverse.parking.model.entity.ParkingLog;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Component
public final class ParkingMapperImpl implements ParkingMapper { // TODO: Нужно разнести логически по разным интерфейсам
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
