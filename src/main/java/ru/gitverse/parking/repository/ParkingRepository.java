package ru.gitverse.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.gitverse.parking.model.dto.ParkingStatsDto;
import ru.gitverse.parking.model.entity.ParkingLog;

import java.sql.Timestamp;
import java.util.Optional;



/**
 * <p>Использую GiST индекс для временного диапазона</p>
 *
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Repository
public interface ParkingRepository extends JpaRepository<ParkingLog, Long> {
    @Query("""
            SELECT p FROM ParkingLog p
            WHERE p.carNumber = :carNumber
            AND p.eventType = 'ENTRY'
            AND NOT EXISTS (
                SELECT 1 FROM ParkingLog e
                WHERE e.eventChainStart = p.eventTime
                AND e.eventType = 'EXIT'
                AND e.carNumber = p.carNumber
            )
            ORDER BY p.eventTime DESC""")
    Optional<ParkingLog> findActiveEntry(@Param("carNumber") String carNumber);


    /**
     * <p>Возвращает статистику парковок, полностью попавших в заданный временной диапазон.</p>
     *
     * <p>Используется оператор <@ (contained by) для фильтрации интервалов стоянок,
     * которые полностью лежат внутри интервала [:startDate, :endDate].</p>
     *
     * @param startDate начальная дата диапазона (включительно)
     * @param endDate   конечная дата диапазона (включительно)
     * @return DTO с количеством въездов, выездов и средним временем парковки (в минутах),
     * учитывающим только полностью лежащие в диапазоне стоянки.
     */
    @Query(value = """
            SELECT
                SUM(CASE WHEN event_type = 'ENTRY' THEN 1 ELSE 0 END) AS entered_count,
                SUM(CASE WHEN event_type = 'EXIT' THEN 1 ELSE 0 END) AS exited_count,
                AVG(CASE WHEN event_type = 'EXIT' THEN EXTRACT(EPOCH FROM (event_time - event_chain_start))/60 ELSE NULL END) AS avg_parking_minutes
            FROM parking_log
            WHERE tstzrange(event_chain_start, event_time) <@ tstzrange(:startDate, :endDate)""", nativeQuery = true)
    ParkingStatsDto getParkingStatsFullyWithinRange(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    /**
     * <p>Возвращает статистику парковок, пересекающихся с заданным временным диапазоном,
     * при этом учитывает только часть времени парковки, попадающую в этот диапазон.</p>
     *
     * <p>Используется оператор && (overlaps) для выбора стоянок,
     * затем для каждой стоянки вычисляется только та часть времени, которая пересекается с [:startDate, :endDate].</p>
     *
     * <p>Среднее время парковки СЧИТАЕТСЯ ИМЕННО ПО ПЕРЕСЕКАЮЩЕМУСЯ УЧАСТКУ (по реально попавшей части стоянки).</p>
     *
     * @param startDate начальная дата диапазона (включительно)
     * @param endDate   конечная дата диапазона (включительно)
     * @return DTO с количеством въездов, выездов и средним временем парковки (в минутах),
     * рассчитанным по частично попавшему времени стоянки.
     */
    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE event_type = 'ENTRY') AS entered_count,
              COUNT(*) FILTER (WHERE event_type = 'EXIT') AS exited_count,
              AVG(
                EXTRACT(EPOCH FROM (
                  LEAST(event_time, :endDate) - GREATEST(event_chain_start, :startDate)
                )) / 60
              ) AS avg_parking_minutes_in_range
            FROM parking_log
            WHERE event_type = 'EXIT'
            AND tstzrange(event_chain_start, event_time) && tstzrange(:startDate, :endDate)""", nativeQuery = true)
    ParkingStatsDto getParkingReportOverlapping(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    /**
     * <p>Возвращает статистику парковок, пересекающихся с заданным временным диапазоном.</p>
     *
     * <p>Используется оператор && (overlaps) для выбора стоянок,
     * которые хоть частично пересекаются с интервалом [:startDate, :endDate].</p>
     *
     * <p>Среднее время парковки считается по ПОЛНОМУ ВРЕМЕНИ СТОЯНКИ (это плохо),
     * без учета частичного попадания.</p>
     *
     * @param startDate начальная дата диапазона (включительно)
     * @param endDate   конечная дата диапазона (включительно)
     * @return DTO с количеством въездов, выездов и средним временем парковки (в минутах)
     */
    @Query(value = """
            SELECT
                SUM(CASE WHEN event_type = 'ENTRY' THEN 1 ELSE 0 END) AS entered_count,
                SUM(CASE WHEN event_type = 'EXIT' THEN 1 ELSE 0 END) AS exited_count,
                AVG(CASE WHEN event_type = 'EXIT' THEN EXTRACT(EPOCH FROM (event_time - event_chain_start))/60 ELSE NULL END) AS avg_parking_minutes
            FROM parking_log
            WHERE tstzrange(event_chain_start, event_time) && tstzrange(:startDate, :endDate)""", nativeQuery = true)
    ParkingStatsDto getParkingStatsOverlappingRange(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    /**
     * Возвращает отчёт по парковке за заданный период:
     * 1. Количество въехавших автомобилей (ENTRY), зафиксированных в пределах интервала.
     * 2. Количество выехавших автомобилей (EXIT), зафиксированных в пределах интервала.
     * 3. Среднее время пребывания на парковке (в минутах), рассчитанное как пересечение временного интервала стоянки
     *    (event_chain_start → event_time) с указанным диапазоном (startDate → endDate).
     *
     * Только события типа EXIT участвуют в подсчёте среднего времени, так как именно они завершают цепочку стоянки.
     *
     * @param startDate начало интересующего интервала
     * @param endDate конец интересующего интервала
     * @return агрегированная статистика по парковке
     */
    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE event_type = 'ENTRY' AND event_time >= :startDate AND event_time <= :endDate) AS entered_count,
              COUNT(*) FILTER (WHERE event_type = 'EXIT'  AND event_time >= :startDate AND event_time <= :endDate) AS exited_count,
              AVG(
                EXTRACT(EPOCH FROM (
                  LEAST(event_time, :endDate) - GREATEST(event_chain_start, :startDate)
                )) / 60
              ) AS avg_parking_minutes_in_range
            FROM parking_log
            WHERE event_type = 'EXIT'
              AND tstzrange(event_chain_start, event_time) && tstzrange(:startDate, :endDate)""", nativeQuery = true)
    ParkingStatsDto getParkingReportAccurate(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
