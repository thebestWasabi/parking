package ru.gitverse.parking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingExitRequest {
    @NotBlank
    private String carNumber;
}
