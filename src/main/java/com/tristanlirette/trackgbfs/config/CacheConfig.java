package com.tristanlirette.trackgbfs.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    CacheManager cacheManager(TrackGbfsProperties properties) {
        var manager = new SimpleCacheManager();
        var ttl = properties.api().cacheTtl();
        manager.setCaches(List.of(
                new CaffeineCache("station-status",
                        caffeineFor(ttl).build()),
                new CaffeineCache("station-history",
                        caffeineWithCapFor(ttl, 200).build()),
                new CaffeineCache("station-information",
                        caffeineFor(ttl).build()),
                new CaffeineCache("station-map",
                        caffeineFor(ttl).build())));
        return manager;
    }

    // Duration.ZERO disables caching (used in tests); otherwise expire after write.
    private static Caffeine<Object, Object> caffeineFor(Duration ttl) {
        return ttl.isZero()
                ? Caffeine.newBuilder().maximumSize(0)
                : Caffeine.newBuilder().expireAfterWrite(ttl);
    }

    // Like caffeineFor but additionally caps at maximumSize when TTL is non-zero.
    // Avoids calling maximumSize twice on the same builder when TTL is zero.
    private static Caffeine<Object, Object> caffeineWithCapFor(Duration ttl, long cap) {
        return ttl.isZero()
                ? Caffeine.newBuilder().maximumSize(0)
                : Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(cap);
    }
}
