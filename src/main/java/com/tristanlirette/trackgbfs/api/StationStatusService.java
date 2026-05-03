package com.tristanlirette.trackgbfs.api;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tristanlirette.trackgbfs.jooq.tables.StationStatus.STATION_STATUS;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;

@Service
public class StationStatusService {

    private final DSLContext dsl;

    StationStatusService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record StationStatusView(
            String stationId,
            int numBikesAvailable,
            Integer numBikesDisabled,
            int numDocksAvailable,
            Integer numDocksDisabled,
            boolean isInstalled,
            boolean isRenting,
            boolean isReturning,
            long lastReported,
            long observedAt) {}

    @Cacheable("station-status")
    public List<StationStatusView> getStationStatus() {
        var ss = STATION_STATUS.as("ss");
        var ss2 = STATION_STATUS.as("ss2");
        return dsl
                .select(ss.STATION_ID,
                        ss.NUM_BIKES_AVAILABLE,
                        ss.NUM_BIKES_DISABLED,
                        ss.NUM_DOCKS_AVAILABLE,
                        ss.NUM_DOCKS_DISABLED,
                        ss.IS_INSTALLED,
                        ss.IS_RENTING,
                        ss.IS_RETURNING,
                        ss.LAST_REPORTED,
                        ss.OBSERVED_AT)
                .from(ss)
                .where(ss.OBSERVED_AT.eq(
                        select(max(ss2.OBSERVED_AT))
                                .from(ss2)
                                .where(ss2.STATION_ID.eq(ss.STATION_ID))))
                .orderBy(ss.LAST_REPORTED.desc())
                .fetch(r -> new StationStatusView(
                        r.get(ss.STATION_ID),
                        r.get(ss.NUM_BIKES_AVAILABLE),
                        r.get(ss.NUM_BIKES_DISABLED),
                        r.get(ss.NUM_DOCKS_AVAILABLE),
                        r.get(ss.NUM_DOCKS_DISABLED),
                        r.get(ss.IS_INSTALLED) != 0,
                        r.get(ss.IS_RENTING) != 0,
                        r.get(ss.IS_RETURNING) != 0,
                        r.get(ss.LAST_REPORTED),
                        r.get(ss.OBSERVED_AT)));
    }

    @Cacheable(value = "station-status", key = "#id")
    public StationStatusView getStationStatus(String id) {
        return dsl
                .select(STATION_STATUS.STATION_ID,
                        STATION_STATUS.NUM_BIKES_AVAILABLE,
                        STATION_STATUS.NUM_BIKES_DISABLED,
                        STATION_STATUS.NUM_DOCKS_AVAILABLE,
                        STATION_STATUS.NUM_DOCKS_DISABLED,
                        STATION_STATUS.IS_INSTALLED,
                        STATION_STATUS.IS_RENTING,
                        STATION_STATUS.IS_RETURNING,
                        STATION_STATUS.LAST_REPORTED,
                        STATION_STATUS.OBSERVED_AT)
                .from(STATION_STATUS)
                .where(STATION_STATUS.STATION_ID.eq(id))
                .orderBy(STATION_STATUS.OBSERVED_AT.desc())
                .limit(1)
                .fetchOne(r -> new StationStatusView(
                        r.get(STATION_STATUS.STATION_ID),
                        r.get(STATION_STATUS.NUM_BIKES_AVAILABLE),
                        r.get(STATION_STATUS.NUM_BIKES_DISABLED),
                        r.get(STATION_STATUS.NUM_DOCKS_AVAILABLE),
                        r.get(STATION_STATUS.NUM_DOCKS_DISABLED),
                        r.get(STATION_STATUS.IS_INSTALLED) != 0,
                        r.get(STATION_STATUS.IS_RENTING) != 0,
                        r.get(STATION_STATUS.IS_RETURNING) != 0,
                        r.get(STATION_STATUS.LAST_REPORTED),
                        r.get(STATION_STATUS.OBSERVED_AT)));
    }

    @Cacheable(value = "station-history", key = "#id")
    public List<StationStatusView> getStationHistory(String id) {
        return dsl
                .select(STATION_STATUS.STATION_ID,
                        STATION_STATUS.NUM_BIKES_AVAILABLE,
                        STATION_STATUS.NUM_BIKES_DISABLED,
                        STATION_STATUS.NUM_DOCKS_AVAILABLE,
                        STATION_STATUS.NUM_DOCKS_DISABLED,
                        STATION_STATUS.IS_INSTALLED,
                        STATION_STATUS.IS_RENTING,
                        STATION_STATUS.IS_RETURNING,
                        STATION_STATUS.LAST_REPORTED,
                        STATION_STATUS.OBSERVED_AT)
                .from(STATION_STATUS)
                .where(STATION_STATUS.STATION_ID.eq(id))
                .orderBy(STATION_STATUS.OBSERVED_AT.desc())
                .fetch(r -> new StationStatusView(
                        r.get(STATION_STATUS.STATION_ID),
                        r.get(STATION_STATUS.NUM_BIKES_AVAILABLE),
                        r.get(STATION_STATUS.NUM_BIKES_DISABLED),
                        r.get(STATION_STATUS.NUM_DOCKS_AVAILABLE),
                        r.get(STATION_STATUS.NUM_DOCKS_DISABLED),
                        r.get(STATION_STATUS.IS_INSTALLED) != 0,
                        r.get(STATION_STATUS.IS_RENTING) != 0,
                        r.get(STATION_STATUS.IS_RETURNING) != 0,
                        r.get(STATION_STATUS.LAST_REPORTED),
                        r.get(STATION_STATUS.OBSERVED_AT)));
    }
}
