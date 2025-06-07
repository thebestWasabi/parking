package ru.gitverse.parking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gitverse.parking.exception.ParkingBusinessException;
import ru.gitverse.parking.exception.ParkingConcurrentOperationException;
import ru.gitverse.parking.mapper.ParkingMapper;
import ru.gitverse.parking.model.dto.ParkingEntryRequest;
import ru.gitverse.parking.model.dto.ParkingExitRequest;
import ru.gitverse.parking.model.dto.ParkingReport;
import ru.gitverse.parking.model.dto.ParkingResponse;
import ru.gitverse.parking.model.dto.ParkingStatsDto;
import ru.gitverse.parking.model.dto.ReportDto;
import ru.gitverse.parking.model.entity.ParkingLog;
import ru.gitverse.parking.repository.ParkingRepository;
import ru.gitverse.parking.util.StringLocker;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {
    private final ParkingRepository parkingRepository;
    private final StringLocker stringLocker;
    private final ParkingMapper parkingMapper;

    @Override
    public ParkingResponse entry(final ParkingEntryRequest request) {  //@NotBlank и NotNull
        final String carNumber = request.getCarNumber();
        log.debug("Регистрация въезда: {}", request.getCarNumber());

        // Теперь нельзя вызвать 2 раза в одном потоке
        if (!stringLocker.tryLock(carNumber)) {
            log.warn("Конфликт операций для автомобиля на въезд: {}", carNumber);
            throw new ParkingConcurrentOperationException("Операция уже выполняется для автомобиля (въезд)" + carNumber);
        }

        try {
            // Проверяю, есть ли уже активный ENTRY (машина на парковке)
            // Не получится 2 раза дёрнуть метод entry и создать 2 парковки с одним номером авто (без предварительного выезда)
            if (parkingRepository.findActiveEntry(carNumber).isPresent()) {
                log.error("Автомобиль уже находится на парковке: {}", carNumber);
                throw new ParkingBusinessException("Автомобиль уже находится на парковке: " + carNumber);
            }

            // Создаю и сохраняю запись о въезде
            final ParkingLog entity = parkingMapper.toEntity(request);
            final ParkingLog saved = parkingRepository.save(entity);
            log.debug("Успешный въезд: carNumber={}, eventTime={}", carNumber, saved.getEventTime());
            return parkingMapper.toResponse(saved);
        }
        finally {
            stringLocker.unlock(carNumber);
        }
    }

    @Override
    public ParkingResponse exit(final ParkingExitRequest request) {
        final String carNumber = request.getCarNumber();
        log.debug("Обработка выезда: {}", carNumber);

        if (!stringLocker.tryLock(carNumber)) {
            log.warn("Конфликт операций для автомобиля на выезд: {}", carNumber);
            throw new ParkingConcurrentOperationException("Операция уже выполняется для автомобиля (выезд)" + carNumber);
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
            stringLocker.unlock(carNumber);
        }
    }

    @Override
    public ParkingReport generateReport(final ReportDto reportDto) {
        final Timestamp start = Timestamp.valueOf(reportDto.getStartTime());
        final Timestamp end = Timestamp.valueOf(reportDto.getEndTime());

        final ParkingStatsDto parkingReport = parkingRepository.getParkingReportAccurate(start, end);

        return new ParkingReport(
                parkingReport.getEnteredCount(),
                parkingReport.getExitedCount(),
                parkingReport.getAvgParkingMinutes() != null ? parkingReport.getAvgParkingMinutes() : 0.0);
    }
}
