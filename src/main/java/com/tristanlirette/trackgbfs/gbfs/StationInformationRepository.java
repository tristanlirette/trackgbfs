package com.tristanlirette.trackgbfs.gbfs;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
class StationInformationRepository {

    private final JdbcClient db;

    StationInformationRepository(JdbcClient db) {
        this.db = db;
    }

    boolean saveIfChanged(StationInformation.Station station, long observedAt) {
        if (findLatest(station.stationId()).map(station::equals).orElse(false)) {
            return false;
        }
        db.sql("""
                INSERT OR IGNORE INTO station_information
                (station_id, name, short_name, lat, lon, address,
                 cross_street, region_id, post_code, rental_methods, capacity, observed_at)
                VALUES (:stationId, :name, :shortName, :lat, :lon, :address,
                        :crossStreet, :regionId, :postCode, :rentalMethods, :capacity, :observedAt)""")
                .param("stationId", station.stationId())
                .param("name", station.name())
                .param("shortName", station.shortName())
                .param("lat", station.lat())
                .param("lon", station.lon())
                .param("address", station.address())
                .param("crossStreet", station.crossStreet())
                .param("regionId", station.regionId())
                .param("postCode", station.postCode())
                .param("rentalMethods", encodeRentalMethods(station.rentalMethods()))
                .param("capacity", station.capacity())
                .param("observedAt", observedAt)
                .update();
        return true;
    }

    Optional<StationInformation.Station> findLatest(String stationId) {
        return db.sql("""
                SELECT station_id, name, short_name, lat, lon, address,
                       cross_street, region_id, post_code, rental_methods, capacity
                FROM station_information
                WHERE station_id = :stationId
                ORDER BY observed_at DESC
                LIMIT 1""")
                .param("stationId", stationId)
                .query(StationInformationRepository::mapRow)
                .optional();
    }

    private static StationInformation.Station mapRow(ResultSet rs, int row) throws SQLException {
        return new StationInformation.Station(
                rs.getString("station_id"),
                rs.getString("name"),
                rs.getString("short_name"),
                rs.getDouble("lat"),
                rs.getDouble("lon"),
                rs.getString("address"),
                rs.getString("cross_street"),
                rs.getString("region_id"),
                rs.getString("post_code"),
                decodeRentalMethods(rs.getString("rental_methods")),
                getNullableInt(rs, "capacity"));
    }

    private static String encodeRentalMethods(List<String> methods) {
        if (methods == null) return null;
        return String.join(",", methods);
    }

    private static List<String> decodeRentalMethods(String encoded) {
        if (encoded == null) return null;
        if (encoded.isEmpty()) return List.of();
        return List.of(encoded.split(",", -1));
    }

    private static Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
