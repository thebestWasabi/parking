package ru.gitverse.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "parking_log")
public class ParkingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String carNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CarType carType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParkingEventType eventType;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private LocalDateTime eventChainStart;
}
