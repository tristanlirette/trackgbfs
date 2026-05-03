package com.tristanlirette.trackgbfs.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TrackGbfsPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsAllFieldsAndConvertsKebabCaseEndpointKeys() {
        runner.withPropertyValues(
                        "trackgbfs.feed.base-url=https://example.test/gbfs/v1",
                        "trackgbfs.feed.language=en",
                        "trackgbfs.poll.default-min-interval=30s",
                        "trackgbfs.poll.min-interval.station-status=5s",
                        "trackgbfs.poll.min-interval.station-information=5m")
                .run(context -> {
                    TrackGbfsProperties props = context.getBean(TrackGbfsProperties.class);

                    assertThat(props.feed().baseUrl()).isEqualTo("https://example.test/gbfs/v1");
                    assertThat(props.feed().language()).isEqualTo("en");
                    assertThat(props.poll().defaultMinInterval()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(props.poll().minInterval())
                            .containsEntry("station-status", Duration.ofSeconds(5))
                            .containsEntry("station-information", Duration.ofMinutes(5));
                });
    }

    @Test
    void minIntervalDefaultsToEmptyMapWhenAbsent() {
        runner.withPropertyValues(
                        "trackgbfs.feed.base-url=https://example.test/gbfs/v1",
                        "trackgbfs.feed.language=en",
                        "trackgbfs.poll.default-min-interval=30s")
                .run(context -> {
                    TrackGbfsProperties props = context.getBean(TrackGbfsProperties.class);
                    assertThat(props.poll().minInterval()).isEmpty();
                });
    }

    @Test
    void blankBaseUrlFailsValidation() {
        runner.withPropertyValues(
                        "trackgbfs.feed.base-url=",
                        "trackgbfs.feed.language=en",
                        "trackgbfs.poll.default-min-interval=30s")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .isInstanceOf(BeanCreationException.class));
    }

    @Test
    void invalidBaseUrlFailsValidation() {
        runner.withPropertyValues(
                        "trackgbfs.feed.base-url=not a url",
                        "trackgbfs.feed.language=en",
                        "trackgbfs.poll.default-min-interval=30s")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .isInstanceOf(BeanCreationException.class));
    }

    @Test
    void missingDefaultMinIntervalFailsValidation() {
        runner.withPropertyValues(
                        "trackgbfs.feed.base-url=https://example.test/gbfs/v1",
                        "trackgbfs.feed.language=en")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .isInstanceOf(BeanCreationException.class));
    }

    @EnableConfigurationProperties(TrackGbfsProperties.class)
    static class TestConfig {
    }
}
