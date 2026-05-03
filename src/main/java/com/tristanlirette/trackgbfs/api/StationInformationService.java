package com.tristanlirette.trackgbfs.api;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tristanlirette.trackgbfs.jooq.tables.StationInformation.STATION_INFORMATION;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;

@Service
public class StationInformationService {

    private final DSLContext dsl;

    StationInformationService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record StationInformationView(
            String stationId,
            String name,
            String shortName,
            double lat,
            double lon,
            String address,
            String crossStreet,
            String regionId,
            String postCode,
            List<String> rentalMethods,
            Integer capacity,
            long observedAt) {}

    @Cacheable("station-information")
    public List<StationInformationView> getStationInformation() {
        var si = STATION_INFORMATION.as("si");
        var si2 = STATION_INFORMATION.as("si2");
        return dsl
                .select(si.STATION_ID, si.NAME, si.SHORT_NAME, si.LAT, si.LON,
                        si.ADDRESS, si.CROSS_STREET, si.REGION_ID, si.POST_CODE,
                        si.RENTAL_METHODS, si.CAPACITY, si.OBSERVED_AT)
                .from(si)
                .where(si.OBSERVED_AT.eq(
                        select(max(si2.OBSERVED_AT))
                                .from(si2)
                                .where(si2.STATION_ID.eq(si.STATION_ID))))
                .orderBy(si.NAME.asc())
                .fetch(r -> toView(
                        r.get(si.STATION_ID), r.get(si.NAME), r.get(si.SHORT_NAME),
                        r.get(si.LAT), r.get(si.LON), r.get(si.ADDRESS),
                        r.get(si.CROSS_STREET), r.get(si.REGION_ID), r.get(si.POST_CODE),
                        r.get(si.RENTAL_METHODS), r.get(si.CAPACITY), r.get(si.OBSERVED_AT)));
    }

    @Cacheable(value = "station-information", key = "#id")
    public StationInformationView getStationInformation(String id) {
        return dsl
                .select(STATION_INFORMATION.STATION_ID, STATION_INFORMATION.NAME, STATION_INFORMATION.SHORT_NAME,
                        STATION_INFORMATION.LAT, STATION_INFORMATION.LON, STATION_INFORMATION.ADDRESS,
                        STATION_INFORMATION.CROSS_STREET, STATION_INFORMATION.REGION_ID, STATION_INFORMATION.POST_CODE,
                        STATION_INFORMATION.RENTAL_METHODS, STATION_INFORMATION.CAPACITY, STATION_INFORMATION.OBSERVED_AT)
                .from(STATION_INFORMATION)
                .where(STATION_INFORMATION.STATION_ID.eq(id))
                .orderBy(STATION_INFORMATION.OBSERVED_AT.desc())
                .limit(1)
                .fetchOne(r -> toView(
                        r.get(STATION_INFORMATION.STATION_ID), r.get(STATION_INFORMATION.NAME), r.get(STATION_INFORMATION.SHORT_NAME),
                        r.get(STATION_INFORMATION.LAT), r.get(STATION_INFORMATION.LON), r.get(STATION_INFORMATION.ADDRESS),
                        r.get(STATION_INFORMATION.CROSS_STREET), r.get(STATION_INFORMATION.REGION_ID), r.get(STATION_INFORMATION.POST_CODE),
                        r.get(STATION_INFORMATION.RENTAL_METHODS), r.get(STATION_INFORMATION.CAPACITY), r.get(STATION_INFORMATION.OBSERVED_AT)));
    }

    private static StationInformationView toView(
            String stationId, String name, String shortName,
            Float lat, Float lon, String address,
            String crossStreet, String regionId, String postCode,
            String rentalMethods, Integer capacity, long observedAt) {
        return new StationInformationView(
                stationId, name, shortName, lat, lon, address,
                crossStreet, regionId, postCode,
                decodeRentalMethods(rentalMethods), capacity, observedAt);
    }

    private static List<String> decodeRentalMethods(String encoded) {
        if (encoded == null) return null;
        if (encoded.isEmpty()) return List.of();
        return List.of(encoded.split(",", -1));
    }
}
