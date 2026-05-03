package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GbfsClientTest {

    private MockRestServiceServer server;
    private GbfsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        var props = new TrackGbfsProperties(
                new TrackGbfsProperties.Feed("https://example.test/gbfs/v1", "en"),
                new TrackGbfsProperties.Poll(Duration.ofSeconds(30), Map.of()),
                null);
        client = new GbfsClient(builder, props);
    }

    @Test
    void buildsTheRequestUrlFromBaseLanguageAndEndpointAndParsesTheEnvelope() {
        String body = """
                {
                  "last_updated": 1777131935,
                  "ttl": 2,
                  "data": {
                    "system_id": "avelo_quebec",
                    "language": "en",
                    "name": "avelo-quebec",
                    "timezone": "America/Montreal",
                    "_station_count": 149
                  }
                }
                """;
        server.expect(requestTo("https://example.test/gbfs/v1/en/system_information"))
                .andExpect(method(GET))
                .andRespond(withSuccess(body, APPLICATION_JSON));

        GbfsResponse<SystemInformation> response = client.fetch("system_information", SystemInformation.class);

        assertThat(response.lastUpdated()).isEqualTo(1777131935L);
        assertThat(response.ttl()).isEqualTo(2L);
        assertThat(response.data().systemId()).isEqualTo("avelo_quebec");
        assertThat(response.data().timezone()).isEqualTo("America/Montreal");

        server.verify();
    }

    @Test
    void propagatesNotFoundForOptionalEndpointThatTheFeedDoesNotPublish() {
        server.expect(requestTo("https://example.test/gbfs/v1/en/free_bike_status"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.fetch("free_bike_status", Object.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
