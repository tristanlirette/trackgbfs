package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class GbfsEndpointProbe implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GbfsEndpointProbe.class);

    static final List<String> KNOWN_ENDPOINTS = List.of(
            "system_information",
            "station_information",
            "station_status",
            "free_bike_status",
            "system_hours",
            "system_calendar",
            "system_regions",
            "system_pricing_plans",
            "system_alerts");

    private final GbfsClient client;
    private final EndpointRegistry registry;
    private final String feedBaseUrl;

    public GbfsEndpointProbe(GbfsClient client, EndpointRegistry registry,
            TrackGbfsProperties properties) {
        this.client = client;
        this.registry = registry;
        this.feedBaseUrl = properties.feed().baseUrl();
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> available = probe();
        if (!available.contains("system_information")) {
            throw new IllegalStateException(
                    "Required GBFS endpoint system_information is not available at " + feedBaseUrl);
        }
        registry.update(available);
        log.info("GBFS endpoints available: {}", available);
    }

    private Set<String> probe() {
        Set<String> available = new LinkedHashSet<>();
        for (String endpoint : KNOWN_ENDPOINTS) {
            try {
                client.fetch(endpoint, Object.class);
                available.add(endpoint);
            } catch (HttpClientErrorException.NotFound e) {
                log.debug("Endpoint {} not published by this feed", endpoint);
            } catch (Exception e) {
                log.warn("Probe failed for endpoint {}: {}", endpoint, e.getMessage());
            }
        }
        return available;
    }
}
