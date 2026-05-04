package com.tristanlirette.trackgbfs.api;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tristanlirette.trackgbfs.jooq.tables.StationInformation.STATION_INFORMATION;
import static com.tristanlirette.trackgbfs.jooq.tables.StationStatus.STATION_STATUS;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;

@Service
public class StationMapService {

    private final DSLContext dsl;

    StationMapService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record StationMapEntry(
            String stationId,
            String name,
            double lat,
            double lon,
            int numBikesAvailable,
            boolean isInstalled) {}

    @Cacheable("station-map")
    public List<StationMapEntry> getStationMap() {
        var si  = STATION_INFORMATION.as("si");
        var si2 = STATION_INFORMATION.as("si2");
        var ss  = STATION_STATUS.as("ss");
        var ss2 = STATION_STATUS.as("ss2");
        return dsl
                .select(si.STATION_ID, si.NAME, si.LAT, si.LON,
                        ss.NUM_BIKES_AVAILABLE, ss.IS_INSTALLED)
                .from(si)
                .join(ss).on(ss.STATION_ID.eq(si.STATION_ID))
                .where(si.OBSERVED_AT.eq(
                        select(max(si2.OBSERVED_AT))
                                .from(si2)
                                .where(si2.STATION_ID.eq(si.STATION_ID))))
                .and(ss.OBSERVED_AT.eq(
                        select(max(ss2.OBSERVED_AT))
                                .from(ss2)
                                .where(ss2.STATION_ID.eq(ss.STATION_ID))))
                .orderBy(si.NAME.asc())
                .fetch(r -> new StationMapEntry(
                        r.get(si.STATION_ID),
                        r.get(si.NAME),
                        r.get(si.LAT),
                        r.get(si.LON),
                        r.get(ss.NUM_BIKES_AVAILABLE),
                        r.get(ss.IS_INSTALLED) != 0));
    }
}
