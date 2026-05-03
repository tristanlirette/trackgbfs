package com.tristanlirette.trackgbfs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station/status")
class StationStatusController {

    private final StationStatusService stationStatusService;

    StationStatusController(StationStatusService stationStatusService) {
        this.stationStatusService = stationStatusService;
    }

    @GetMapping()
    List<StationStatusService.StationStatusView> getStationStatus() {
        return stationStatusService.getStationStatus();
    }

    @GetMapping("/{id}")
    ResponseEntity<StationStatusService.StationStatusView> getStationStatus(@PathVariable String id) {
        StationStatusService.StationStatusView result = stationStatusService.getStationStatus(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/history")
    List<StationStatusService.StationStatusView> getStationHistory(@PathVariable String id) {
        return stationStatusService.getStationHistory(id);
    }
}
