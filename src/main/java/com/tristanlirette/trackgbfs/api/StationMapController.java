package com.tristanlirette.trackgbfs.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station/map")
class StationMapController {

    private final StationMapService stationMapService;

    StationMapController(StationMapService stationMapService) {
        this.stationMapService = stationMapService;
    }

    @GetMapping
    List<StationMapService.StationMapEntry> getStationMap() {
        return stationMapService.getStationMap();
    }
}
