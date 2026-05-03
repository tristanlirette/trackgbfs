package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.ApplicationArguments;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class StationPollerTest {

    static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    static final Duration MIN_INFO = Duration.ofMinutes(5);
    static final Duration MIN_STATUS = Duration.ofSeconds(5);

    @Mock TaskScheduler scheduler;
    @Mock GbfsClient client;
    @Mock EndpointRegistry registry;
    @Mock StationInformationRepository stationInfoRepo;
    @Mock StationStatusRepository stationStatusRepo;

    StationPoller poller;

    @BeforeEach
    void setUp() {
        var props = new TrackGbfsProperties(
                new TrackGbfsProperties.Feed("https://example.test/gbfs/v1", "en"),
                new TrackGbfsProperties.Poll(Duration.ofSeconds(30), Map.of(
                        "station-information", MIN_INFO,
                        "station-status", MIN_STATUS)),
                null);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        poller = new StationPoller(scheduler, clock, client, registry, stationInfoRepo, stationStatusRepo, props);
    }

    @Test
    void run_schedulesImmediatePollForBothEndpoints() {
        poller.run(mock(ApplicationArguments.class));

        verify(scheduler, times(2)).schedule(any(Runnable.class), eq(NOW));
    }

    @Test
    void pollStationInformation_skipsWhenEndpointUnavailable() {
        when(registry.isAvailable("station_information")).thenReturn(false);

        poller.pollStationInformation();

        verifyNoInteractions(client, stationInfoRepo);
        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(MIN_INFO)));
    }

    @Test
    void pollStationStatus_skipsWhenEndpointUnavailable() {
        when(registry.isAvailable("station_status")).thenReturn(false);

        poller.pollStationStatus();

        verifyNoInteractions(client, stationStatusRepo);
        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(MIN_STATUS)));
    }

    @Test
    void pollStationInformation_savesEachStation() {
        when(registry.isAvailable("station_information")).thenReturn(true);
        StationInformation.Station s1 = new StationInformation.Station(
                "s1", "S1", null, 45.5, -73.5, null, null, null, null, null, null);
        StationInformation.Station s2 = new StationInformation.Station(
                "s2", "S2", null, 45.6, -73.6, null, null, null, null, null, null);
        GbfsResponse<StationInformation> resp = new GbfsResponse<>(
                1000L, 30L, null, new StationInformation(List.of(s1, s2)));
        when(client.fetch("station_information", StationInformation.class)).thenReturn(resp);

        poller.pollStationInformation();

        verify(stationInfoRepo).saveIfChanged(s1, 1000L);
        verify(stationInfoRepo).saveIfChanged(s2, 1000L);
    }

    @Test
    void pollStationInformation_usesTtlWhenLongerThanMinInterval() {
        when(registry.isAvailable("station_information")).thenReturn(true);
        StationInformation.Station s = new StationInformation.Station(
                "s1", "S1", null, 0, 0, null, null, null, null, null, null);
        // TTL 600s > minInterval 300s → next poll at NOW + 600s
        GbfsResponse<StationInformation> resp = new GbfsResponse<>(
                1000L, 600L, null, new StationInformation(List.of(s)));
        when(client.fetch("station_information", StationInformation.class)).thenReturn(resp);

        poller.pollStationInformation();

        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(Duration.ofSeconds(600))));
    }

    @Test
    void pollStationInformation_usesMinIntervalWhenLongerThanTtl() {
        when(registry.isAvailable("station_information")).thenReturn(true);
        StationInformation.Station s = new StationInformation.Station(
                "s1", "S1", null, 0, 0, null, null, null, null, null, null);
        // TTL 10s < minInterval 300s → next poll at NOW + 300s
        GbfsResponse<StationInformation> resp = new GbfsResponse<>(
                1000L, 10L, null, new StationInformation(List.of(s)));
        when(client.fetch("station_information", StationInformation.class)).thenReturn(resp);

        poller.pollStationInformation();

        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(MIN_INFO)));
    }

    @Test
    void pollStationInformation_reschedulesAtMinIntervalOnError() {
        when(registry.isAvailable("station_information")).thenReturn(true);
        when(client.fetch(any(), any())).thenThrow(new RuntimeException("connection refused"));

        assertThatCode(() -> poller.pollStationInformation()).doesNotThrowAnyException();

        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(MIN_INFO)));
    }

    @Test
    void pollStationStatus_savesEachEntry() {
        when(registry.isAvailable("station_status")).thenReturn(true);
        StationStatus.Entry e = new StationStatus.Entry(
                "s1", 5, null, 10, null, true, true, true, 1777000000L);
        GbfsResponse<StationStatus> resp = new GbfsResponse<>(
                1000L, 30L, null, new StationStatus(List.of(e)));
        when(client.fetch("station_status", StationStatus.class)).thenReturn(resp);

        poller.pollStationStatus();

        verify(stationStatusRepo).saveIfChanged(e, 1000L);
    }

    @Test
    void pollStationStatus_reschedulesAtMinIntervalOnError() {
        when(registry.isAvailable("station_status")).thenReturn(true);
        when(client.fetch(any(), any())).thenThrow(new RuntimeException("timeout"));

        assertThatCode(() -> poller.pollStationStatus()).doesNotThrowAnyException();

        verify(scheduler).schedule(any(Runnable.class), eq(NOW.plus(MIN_STATUS)));
    }
}
