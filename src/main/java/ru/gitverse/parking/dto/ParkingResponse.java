package ru.gitverse.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingResponse {
    private LocalDateTime dateTime;
}
