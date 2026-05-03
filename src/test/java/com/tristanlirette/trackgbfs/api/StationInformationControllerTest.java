package com.tristanlirette.trackgbfs.api;

import com.tristanlirette.trackgbfs.gbfs.GbfsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class StationInformationControllerTest {

    @MockitoBean
    GbfsClient gbfsClient;

    @Autowired WebApplicationContext context;
    @Autowired JdbcClient db;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getStationInformation_returnsEmptyArrayWhenNoData() throws Exception {
        mvc.perform(get("/station/information"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getStationInformation_returnsLatestInfo() throws Exception {
        insertInformation("s1", "Station 1", 45.5, -73.5, 1000L);

        mvc.perform(get("/station/information"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].station_id").value("s1"))
                .andExpect(jsonPath("$[0].name").value("Station 1"));
    }

    @Test
    void getStationInformation_returnsOnlyLatestInfoPerStation() throws Exception {
        insertInformation("s1", "Station 1 Old", 45.5, -73.5, 1000L);
        insertInformation("s1", "Station 1 New", 45.6, -73.6, 2000L);

        mvc.perform(get("/station/information"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Station 1 New"));
    }

    @Test
    void getStationInformation_returnsInfoForAllStations() throws Exception {
        insertInformation("s1", "Alpha", 45.5, -73.5, 1000L);
        insertInformation("s2", "Beta", 45.6, -73.6, 2000L);

        mvc.perform(get("/station/information"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getStationInformation_orderedByNameAsc() throws Exception {
        insertInformation("s2", "Beta", 45.6, -73.6, 1000L);
        insertInformation("s1", "Alpha", 45.5, -73.5, 2000L);

        mvc.perform(get("/station/information"))
                .andExpect(jsonPath("$[0].station_id").value("s1"))
                .andExpect(jsonPath("$[1].station_id").value("s2"));
    }

    @Test
    void getStationInformationById_returnsLatestInfo() throws Exception {
        insertInformation("s1", "Station 1", 45.5, -73.5, 1000L);

        mvc.perform(get("/station/information/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.station_id").value("s1"))
                .andExpect(jsonPath("$.name").value("Station 1"));
    }

    @Test
    void getStationInformationById_returns404WhenNotFound() throws Exception {
        mvc.perform(get("/station/information/unknown"))
                .andExpect(status().isNotFound());
    }

    private void insertInformation(String id, String name, double lat, double lon, long observedAt) {
        db.sql("""
                INSERT INTO station_information
                (station_id, name, lat, lon, observed_at)
                VALUES (:id, :name, :lat, :lon, :observedAt)""")
                .param("id", id).param("name", name)
                .param("lat", lat).param("lon", lon)
                .param("observedAt", observedAt)
                .update();
    }
}
