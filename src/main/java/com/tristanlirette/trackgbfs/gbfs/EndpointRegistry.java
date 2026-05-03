package com.tristanlirette.trackgbfs.gbfs;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EndpointRegistry {

    private volatile Set<String> available = Set.of();

    public boolean isAvailable(String endpoint) {
        return available.contains(endpoint);
    }

    public Set<String> availableEndpoints() {
        return available;
    }

    void update(Set<String> nowAvailable) {
        available = Set.copyOf(nowAvailable);
    }
}
