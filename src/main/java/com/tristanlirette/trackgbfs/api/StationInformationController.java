package com.tristanlirette.trackgbfs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station/information")
class StationInformationController {

    private final StationInformationService stationInformationService;

    StationInformationController(StationInformationService stationInformationService) {
        this.stationInformationService = stationInformationService;
    }

    @GetMapping()
    List<StationInformationService.StationInformationView> getStationInformation() {
        return stationInformationService.getStationInformation();
    }

    @GetMapping("/{id}")
    ResponseEntity<StationInformationService.StationInformationView> getStationInformation(@PathVariable String id) {
        StationInformationService.StationInformationView result = stationInformationService.getStationInformation(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }
}
