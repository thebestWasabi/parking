package ru.gitverse.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gitverse.parking.entity.ParkingLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Repository
public interface ParkingRepository extends JpaRepository<ParkingLog, Long> {
    @Query("""
            SELECT p FROM ParkingLog p
            WHERE p.carNumber = :carNumber
            AND p.eventType = 'ENTRY'
            AND p.eventChainStart = p.eventTime
            ORDER BY p.eventTime DESC""")
    Optional<ParkingLog> findActiveEntry(@RequestParam("carNumber") String carNumber);

    @Query("""
            SELECT p FROM ParkingLog p
            WHERE p.eventTime BETWEEN :start AND :end""")
    List<ParkingLog> findEventsBetween(LocalDateTime start, LocalDateTime end);
}
