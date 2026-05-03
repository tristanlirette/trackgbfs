package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
@Order(2)
class StationPoller implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StationPoller.class);

    private final TaskScheduler scheduler;
    private final Clock clock;
    private final GbfsClient client;
    private final EndpointRegistry registry;
    private final StationInformationRepository stationInfoRepo;
    private final StationStatusRepository stationStatusRepo;
    private final Duration stationInfoMinInterval;
    private final Duration stationStatusMinInterval;

    StationPoller(TaskScheduler scheduler, Clock clock, GbfsClient client,
            EndpointRegistry registry,
            StationInformationRepository stationInfoRepo,
            StationStatusRepository stationStatusRepo,
            TrackGbfsProperties properties) {
        this.scheduler = scheduler;
        this.clock = clock;
        this.client = client;
        this.registry = registry;
        this.stationInfoRepo = stationInfoRepo;
        this.stationStatusRepo = stationStatusRepo;
        TrackGbfsProperties.Poll poll = properties.poll();
        this.stationInfoMinInterval = poll.minInterval()
                .getOrDefault("station-information", poll.defaultMinInterval());
        this.stationStatusMinInterval = poll.minInterval()
                .getOrDefault("station-status", poll.defaultMinInterval());
    }

    @Override
    public void run(ApplicationArguments args) {
        scheduler.schedule(this::pollStationInformation, clock.instant());
        scheduler.schedule(this::pollStationStatus, clock.instant());
    }

    void pollStationInformation() {
        Duration next = stationInfoMinInterval;
        if (registry.isAvailable("station_information")) {
            try {
                GbfsResponse<StationInformation> resp =
                        client.fetch("station_information", StationInformation.class);
                int saved = 0;
                for (StationInformation.Station s : resp.data().stations()) {
                    if (stationInfoRepo.saveIfChanged(s, resp.lastUpdated())) saved++;
                }
                log.info("station_information: {}/{} stations updated",
                        saved, resp.data().stations().size());
                Duration ttl = Duration.ofSeconds(resp.ttl());
                if (ttl.compareTo(next) > 0) next = ttl;
            } catch (Exception e) {
                log.warn("Failed to poll station_information: {}", e.getMessage());
            }
        }
        scheduler.schedule(this::pollStationInformation, clock.instant().plus(next));
    }

    void pollStationStatus() {
        Duration next = stationStatusMinInterval;
        if (registry.isAvailable("station_status")) {
            try {
                GbfsResponse<StationStatus> resp =
                        client.fetch("station_status", StationStatus.class);
                int saved = 0;
                for (StationStatus.Entry e : resp.data().stations()) {
                    if (stationStatusRepo.saveIfChanged(e, resp.lastUpdated())) saved++;
                }
                log.info("station_status: {}/{} stations updated",
                        saved, resp.data().stations().size());
                Duration ttl = Duration.ofSeconds(resp.ttl());
                if (ttl.compareTo(next) > 0) next = ttl;
            } catch (Exception e) {
                log.warn("Failed to poll station_status: {}", e.getMessage());
            }
        }
        scheduler.schedule(this::pollStationStatus, clock.instant().plus(next));
    }
}
