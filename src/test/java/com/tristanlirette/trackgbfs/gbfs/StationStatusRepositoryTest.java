package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.gbfs.StationStatus.Entry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StationStatusRepositoryTest {

    @MockitoBean
    GbfsClient gbfsClient;

    @Autowired
    StationStatusRepository repo;

    private static final Entry ENTRY = new Entry(
            "s1", 5, null, 10, null, true, true, true, 1777000000L);

    @Test
    void insertsFirstSnapshotForNewStation() {
        boolean saved = repo.saveIfChanged(ENTRY, 1000L);

        assertThat(saved).isTrue();
        assertThat(repo.findLatest("s1")).contains(ENTRY);
    }

    @Test
    void skipsInsertWhenDataUnchanged() {
        repo.saveIfChanged(ENTRY, 1000L);

        boolean saved = repo.saveIfChanged(ENTRY, 1030L);

        assertThat(saved).isFalse();
    }

    @Test
    void insertsNewSnapshotWhenBikeCountChanges() {
        Entry updated = new Entry("s1", 3, null, 12, null, true, true, true, 1777000000L);
        repo.saveIfChanged(ENTRY, 1000L);

        boolean saved = repo.saveIfChanged(updated, 2000L);

        assertThat(saved).isTrue();
        assertThat(repo.findLatest("s1")).contains(updated);
    }

    @Test
    void returnsEmptyForUnknownStation() {
        assertThat(repo.findLatest("unknown")).isEmpty();
    }

    @Test
    void roundTripsNullableDisabledCounts() {
        Entry withDisabled = new Entry("s2", 4, 1, 8, 2, true, true, false, 1777000000L);
        repo.saveIfChanged(withDisabled, 1000L);

        assertThat(repo.findLatest("s2")).contains(withDisabled);
    }
}
