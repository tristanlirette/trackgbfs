package com.tristanlirette.trackgbfs.gbfs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StationStatus(List<Entry> stations) {

    public record Entry(
            @JsonProperty("station_id") String stationId,
            @JsonProperty("num_bikes_available") int numBikesAvailable,
            @JsonProperty("num_bikes_disabled") Integer numBikesDisabled,
            @JsonProperty("num_docks_available") int numDocksAvailable,
            @JsonProperty("num_docks_disabled") Integer numDocksDisabled,
            @JsonProperty("is_installed") boolean isInstalled,
            @JsonProperty("is_renting") boolean isRenting,
            @JsonProperty("is_returning") boolean isReturning,
            @JsonProperty("last_reported") long lastReported) {
    }
}
