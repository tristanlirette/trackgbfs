package com.tristanlirette.trackgbfs.gbfs;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
class StationStatusRepository {

    private final JdbcClient db;

    StationStatusRepository(JdbcClient db) {
        this.db = db;
    }

    boolean saveIfChanged(StationStatus.Entry entry, long observedAt) {
        if (findLatest(entry.stationId()).map(entry::equals).orElse(false)) {
            return false;
        }
        db.sql("""
                INSERT OR IGNORE INTO station_status
                (station_id, num_bikes_available, num_bikes_disabled,
                 num_docks_available, num_docks_disabled,
                 is_installed, is_renting, is_returning, last_reported, observed_at)
                VALUES (:stationId, :numBikesAvailable, :numBikesDisabled,
                        :numDocksAvailable, :numDocksDisabled,
                        :isInstalled, :isRenting, :isReturning, :lastReported, :observedAt)""")
                .param("stationId", entry.stationId())
                .param("numBikesAvailable", entry.numBikesAvailable())
                .param("numBikesDisabled", entry.numBikesDisabled())
                .param("numDocksAvailable", entry.numDocksAvailable())
                .param("numDocksDisabled", entry.numDocksDisabled())
                .param("isInstalled", entry.isInstalled())
                .param("isRenting", entry.isRenting())
                .param("isReturning", entry.isReturning())
                .param("lastReported", entry.lastReported())
                .param("observedAt", observedAt)
                .update();
        return true;
    }

    Optional<StationStatus.Entry> findLatest(String stationId) {
        return db.sql("""
                SELECT station_id, num_bikes_available, num_bikes_disabled,
                       num_docks_available, num_docks_disabled,
                       is_installed, is_renting, is_returning, last_reported
                FROM station_status
                WHERE station_id = :stationId
                ORDER BY observed_at DESC
                LIMIT 1""")
                .param("stationId", stationId)
                .query(StationStatusRepository::mapRow)
                .optional();
    }

    private static StationStatus.Entry mapRow(ResultSet rs, int row) throws SQLException {
        return new StationStatus.Entry(
                rs.getString("station_id"),
                rs.getInt("num_bikes_available"),
                getNullableInt(rs, "num_bikes_disabled"),
                rs.getInt("num_docks_available"),
                getNullableInt(rs, "num_docks_disabled"),
                rs.getBoolean("is_installed"),
                rs.getBoolean("is_renting"),
                rs.getBoolean("is_returning"),
                rs.getLong("last_reported"));
    }

    private static Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
