package ru.gitverse.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gitverse.parking.entity.CarType;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingEntryRequest {
    @NotBlank
    private String carNumber;

    @NotNull
    private CarType carType;
}
