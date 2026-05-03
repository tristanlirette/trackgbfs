package com.tristanlirette.trackgbfs.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "trackgbfs")
@Validated
public record TrackGbfsProperties(
        @Valid @NotNull Feed feed,
        @Valid @NotNull Poll poll,
        @Valid Api api) {
    public TrackGbfsProperties {
        api = api == null ? new Api(null) : api;
    }

    public record Feed(
            @NotBlank @URL String baseUrl,
            @NotBlank String language) {
    }

    public record Api(
            @NotNull Duration cacheTtl) {
        public Api {
            cacheTtl = cacheTtl == null ? Duration.ofSeconds(30) : cacheTtl;
        }
    }

    public record Poll(
            @NotNull Duration defaultMinInterval,
            Map<String, Duration> minInterval) {
        public Poll {
            minInterval = minInterval == null ? Map.of() : Map.copyOf(minInterval);
        }
    }
}
