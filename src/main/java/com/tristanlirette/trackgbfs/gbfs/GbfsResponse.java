package com.tristanlirette.trackgbfs.gbfs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GbfsResponse<T>(
        @JsonProperty("last_updated") long lastUpdated,
        long ttl,
        String version,
        T data) {
}
