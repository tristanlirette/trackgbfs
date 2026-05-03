package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.gbfs.StationInformation.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StationInformationRepositoryTest {

    @MockitoBean
    GbfsClient gbfsClient;

    @Autowired
    StationInformationRepository repo;

    private static final Station STATION = new Station(
            "s1", "Station One", "S1", 45.5, -73.5, "123 Main St",
            null, null, null, List.of("KEY"), 20);

    @Test
    void insertsFirstSnapshotForNewStation() {
        boolean saved = repo.saveIfChanged(STATION, 1000L);

        assertThat(saved).isTrue();
        assertThat(repo.findLatest("s1")).contains(STATION);
    }

    @Test
    void skipsInsertWhenDataUnchanged() {
        repo.saveIfChanged(STATION, 1000L);

        boolean saved = repo.saveIfChanged(STATION, 1030L);

        assertThat(saved).isFalse();
    }

    @Test
    void insertsNewSnapshotWhenDataChanges() {
        Station updated = new Station("s1", "Station One", "S1", 45.5, -73.5, "123 Main St",
                null, null, null, List.of("KEY"), 25);
        repo.saveIfChanged(STATION, 1000L);

        boolean saved = repo.saveIfChanged(updated, 2000L);

        assertThat(saved).isTrue();
        assertThat(repo.findLatest("s1")).contains(updated);
    }

    @Test
    void returnsEmptyForUnknownStation() {
        assertThat(repo.findLatest("unknown")).isEmpty();
    }

    @Test
    void roundTripsNullableFields() {
        Station minimal = new Station("s2", "Minimal", null, 45.0, -73.0,
                null, null, null, null, null, null);
        repo.saveIfChanged(minimal, 1000L);

        assertThat(repo.findLatest("s2")).contains(minimal);
    }

    @Test
    void roundTripsRentalMethods() {
        Station withMethods = new Station("s3", "Multi Method", null, 45.0, -73.0,
                null, null, null, null, List.of("KEY", "CREDITCARD"), null);
        repo.saveIfChanged(withMethods, 1000L);

        assertThat(repo.findLatest("s3")).contains(withMethods);
    }
}
