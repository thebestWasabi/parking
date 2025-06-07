package ru.gitverse.parking.mapper;

import ru.gitverse.parking.model.dto.ParkingEntryRequest;
import ru.gitverse.parking.model.dto.ParkingResponse;
import ru.gitverse.parking.model.entity.ParkingLog;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
public interface ParkingMapper {
    ParkingLog toEntity(ParkingEntryRequest request);

    ParkingResponse toResponse(ParkingLog parkingLog);

    ParkingLog toExitEvent(ParkingLog parkingLog);
}
