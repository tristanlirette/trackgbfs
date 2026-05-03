package com.tristanlirette.trackgbfs.gbfs;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class GbfsParsingTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void parsesQuebecSystemInformationIgnoringUnderscoreExtensions() throws IOException {
        GbfsResponse<SystemInformation> response = read(
                "fixtures/gbfs/system_information.json",
                new TypeReference<>() {
                });

        assertThat(response.lastUpdated()).isEqualTo(1777131935L);
        assertThat(response.ttl()).isEqualTo(2L);
        assertThat(response.version()).isNull();

        SystemInformation data = response.data();
        assertThat(data.systemId()).isEqualTo("avelo_quebec");
        assertThat(data.language()).isEqualTo("en");
        assertThat(data.name()).isEqualTo("avelo-quebec");
        assertThat(data.timezone()).isEqualTo("America/Montreal");
        assertThat(data.phoneNumber()).isEqualTo("418-627-2511");
        assertThat(data.email()).isEqualTo("Contact par téléphone seulement ");
    }

    @Test
    void parsesStationInformation() throws IOException {
        GbfsResponse<StationInformation> response = read(
                "fixtures/gbfs/station_information.json",
                new TypeReference<>() {
                });

        assertThat(response.version()).isEqualTo("1.1");
        assertThat(response.data().stations()).hasSize(2);

        StationInformation.Station first = response.data().stations().get(0);
        assertThat(first.stationId()).isEqualTo("1");
        assertThat(first.name()).isEqualTo("Place D'Youville");
        assertThat(first.lat()).isEqualTo(46.81306);
        assertThat(first.lon()).isEqualTo(-71.21333);
        assertThat(first.address()).isEqualTo("1 rue D'Youville");
        assertThat(first.capacity()).isEqualTo(19);
        assertThat(first.rentalMethods()).containsExactly("KEY", "CREDITCARD");

        StationInformation.Station second = response.data().stations().get(1);
        assertThat(second.address()).isNull();
        assertThat(second.rentalMethods()).isNull();
        assertThat(second.capacity()).isEqualTo(15);
    }

    @Test
    void parsesStationStatusCoercingNumericBooleansAndPreservingOptionalIntegers() throws IOException {
        GbfsResponse<StationStatus> response = read(
                "fixtures/gbfs/station_status.json",
                new TypeReference<>() {
                });

        assertThat(response.data().stations()).hasSize(2);

        StationStatus.Entry first = response.data().stations().get(0);
        assertThat(first.stationId()).isEqualTo("1");
        assertThat(first.numBikesAvailable()).isEqualTo(7);
        assertThat(first.numBikesDisabled()).isEqualTo(1);
        assertThat(first.numDocksAvailable()).isEqualTo(11);
        assertThat(first.numDocksDisabled()).isEqualTo(0);
        assertThat(first.isInstalled()).isTrue();
        assertThat(first.isRenting()).isTrue();
        assertThat(first.isReturning()).isTrue();
        assertThat(first.lastReported()).isEqualTo(1777131930L);

        StationStatus.Entry second = response.data().stations().get(1);
        assertThat(second.numBikesAvailable()).isZero();
        assertThat(second.numBikesDisabled()).isNull();
        assertThat(second.numDocksDisabled()).isNull();
        assertThat(second.isRenting()).isFalse();
        assertThat(second.isReturning()).isTrue();
    }

    private <T> T read(String resource, TypeReference<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return mapper.readValue(in, type);
        }
    }
}
