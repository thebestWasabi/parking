package ru.gitverse.parking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gitverse.parking.dto.ParkingEntryRequest;
import ru.gitverse.parking.dto.ParkingExitRequest;
import ru.gitverse.parking.dto.ParkingReport;
import ru.gitverse.parking.dto.ParkingResponse;
import ru.gitverse.parking.entity.ParkingEventType;
import ru.gitverse.parking.entity.ParkingLog;
import ru.gitverse.parking.exception.ConcurrentParkingOperationException;
import ru.gitverse.parking.exception.ParkingBusinessException;
import ru.gitverse.parking.mapper.ParkingMapper;
import ru.gitverse.parking.repository.ParkingRepository;
import ru.gitverse.parking.util.ParkingLocker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultParkingService implements ParkingService {
    private final ParkingRepository parkingRepository;
    private final ParkingLocker parkingLocker;
    private final ParkingMapper parkingMapper;

    @Override
    public ParkingResponse entry(final ParkingEntryRequest request) {  //@NotBlank и NotNull
        final String carNumber = request.getCarNumber();
        log.debug("Регистрация въезда: {}", request.getCarNumber());

        if (!parkingLocker.tryLock(carNumber)) {
            log.warn("Конфликт операций для автомобиля: {}", carNumber);
            throw new ConcurrentParkingOperationException("Операция уже выполняется");
        }

        try {
            final ParkingLog entity = parkingMapper.toEntity(request);
            final ParkingLog saved = parkingRepository.save(entity);
            log.debug("Успешный въезд: carNumber={}, eventTime={}", carNumber, saved.getEventTime());
            return parkingMapper.toResponse(saved);
        }
        finally {
            parkingLocker.unlock(carNumber);
        }
    }

    @Override
    public ParkingResponse exit(final ParkingExitRequest request) {
        final String carNumber = request.getCarNumber();
        log.debug("Обработка выезда: {}", carNumber);

        if (!parkingLocker.tryLock(carNumber)) {
            log.warn("Конфликт операций для автомобиля: {}", carNumber);
            throw new ConcurrentParkingOperationException("Операция уже выполняется");
        }

        try {
            final ParkingLog lastEntry = parkingRepository.findActiveEntry(carNumber)
                    .orElseThrow(() -> {
                        log.error("Автомобиль не найден на парковке: {}", carNumber);
                        return new ParkingBusinessException("Автомобиль не найден на парковке");
                    });

            final ParkingLog exitEvent = parkingMapper.toExitEvent(lastEntry);
            final ParkingLog saved = parkingRepository.save(exitEvent);

            log.debug("Успешный выезд: carNumber={}, duration={} минут",
                    carNumber, Duration.between(lastEntry.getEventTime(), saved.getEventTime()).toMinutes()
            );

            return parkingMapper.toResponse(saved);
        }
        finally {
            parkingLocker.unlock(carNumber);
        }
    }

    @Override
    public ParkingReport generateReport(final LocalDateTime start, final LocalDateTime end) {
        final List<ParkingLog> events = parkingRepository.findEventsBetween(start, end);

        final int entered = (int) events.stream()
                .filter(e -> e.getEventType() == ParkingEventType.ENTRY)
                .count();

        final int exited = (int) events.stream()
                .filter(e -> e.getEventType() == ParkingEventType.EXIT)
                .count();

        final double avgMinutes = events.stream()
                .filter(e -> e.getEventType() == ParkingEventType.EXIT)
                .mapToLong(e -> Duration.between(e.getEventChainStart(), e.getEventTime()).toMinutes())
                .average()
                .orElse(0);

        return new ParkingReport(entered, exited, avgMinutes);
    }
}
