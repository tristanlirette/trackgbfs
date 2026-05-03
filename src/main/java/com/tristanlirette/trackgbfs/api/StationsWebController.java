package com.tristanlirette.trackgbfs.api;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RegisterReflectionForBinding(StationsWebController.StationRow.class)
@Controller
class StationsWebController {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private final StationStatusService stationStatusService;

    StationsWebController(StationStatusService stationStatusService) {
        this.stationStatusService = stationStatusService;
    }

    record StationRow(
            String stationId,
            int numBikesAvailable,
            Integer numBikesDisabled,
            int numDocksAvailable,
            Integer numDocksDisabled,
            boolean isInstalled,
            boolean isRenting,
            boolean isReturning,
            String lastReported,
            String observedAt) {}

    @GetMapping("/")
    String stations(Model model) {
        List<StationRow> rows = stationStatusService.getStationStatus().stream()
                .map(v -> new StationRow(
                        v.stationId(),
                        v.numBikesAvailable(),
                        v.numBikesDisabled(),
                        v.numDocksAvailable(),
                        v.numDocksDisabled(),
                        v.isInstalled(),
                        v.isRenting(),
                        v.isReturning(),
                        FMT.format(Instant.ofEpochSecond(v.lastReported())),
                        FMT.format(Instant.ofEpochSecond(v.observedAt()))))
                .toList();
        model.addAttribute("rows", rows);
        return "stations";
    }
}
