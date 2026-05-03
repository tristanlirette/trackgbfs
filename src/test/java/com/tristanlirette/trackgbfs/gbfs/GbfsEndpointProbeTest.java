package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class GbfsEndpointProbeTest {

    @Mock
    GbfsClient client;

    @Mock
    EndpointRegistry registry;

    @Mock
    ApplicationArguments args;

    GbfsEndpointProbe probe;

    @BeforeEach
    void setUp() {
        var props = new TrackGbfsProperties(
                new TrackGbfsProperties.Feed("https://example.test/gbfs/v1", "en"),
                new TrackGbfsProperties.Poll(Duration.ofSeconds(30), Map.of()),
                null);
        probe = new GbfsEndpointProbe(client, registry, props);
    }

    @Test
    void startupRegistersAvailableEndpointsAndIgnores404s() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(client).fetch(eq("free_bike_status"), any());
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(client).fetch(eq("system_hours"), any());

        probe.run(args);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(registry).update(captor.capture());

        Set<String> registered = captor.getValue();
        assertThat(registered).contains("system_information", "station_information", "station_status");
        assertThat(registered).doesNotContain("free_bike_status", "system_hours");
    }

    @Test
    void startupFailsIfSystemInformationIsNotAvailable() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(client).fetch(eq("system_information"), any());

        assertThatThrownBy(() -> probe.run(args))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("system_information");
    }

}
