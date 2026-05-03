package com.tristanlirette.trackgbfs.gbfs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record StationInformation(List<Station> stations) {

    public record Station(
            @JsonProperty("station_id") String stationId,
            String name,
            @JsonProperty("short_name") String shortName,
            double lat,
            double lon,
            String address,
            @JsonProperty("cross_street") String crossStreet,
            @JsonProperty("region_id") String regionId,
            @JsonProperty("post_code") String postCode,
            @JsonProperty("rental_methods") List<String> rentalMethods,
            Integer capacity) {
    }
}
