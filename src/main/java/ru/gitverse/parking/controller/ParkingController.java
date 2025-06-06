package ru.gitverse.parking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gitverse.parking.dto.ParkingEntryRequest;
import ru.gitverse.parking.dto.ParkingExitRequest;
import ru.gitverse.parking.dto.ParkingReport;
import ru.gitverse.parking.dto.ParkingResponse;
import ru.gitverse.parking.service.ParkingService;

import java.time.LocalDateTime;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<ParkingResponse> registerEntry(@Valid @RequestBody final ParkingEntryRequest request) {
        final ParkingResponse responseDto = parkingService.entry(request);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/exit")
    public ResponseEntity<ParkingResponse> registerExit(@Valid @RequestBody final ParkingExitRequest request) {
        final ParkingResponse responseDto = parkingService.exit(request);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/report")
    public ResponseEntity<ParkingReport> getReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime start,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime end) {
        return ResponseEntity.ok(parkingService.generateReport(start, end));
    }
}
