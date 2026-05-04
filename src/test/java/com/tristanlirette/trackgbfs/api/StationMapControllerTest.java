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
class StationMapControllerTest {

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
    void getStationMap_returnsEmptyArrayWhenNoData() throws Exception {
        mvc.perform(get("/station/map"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getStationMap_returnsMergedData() throws Exception {
        insertInformation("s1", "Station One", 45.5f, -73.5f, 1000L);
        insertStatus("s1", 3, 1000L);

        mvc.perform(get("/station/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].station_id").value("s1"))
                .andExpect(jsonPath("$[0].name").value("Station One"))
                .andExpect(jsonPath("$[0].num_bikes_available").value(3))
                .andExpect(jsonPath("$[0].is_installed").value(true));
    }

    @Test
    void getStationMap_excludesStationWithNoStatus() throws Exception {
        insertInformation("s1", "Station One", 45.5f, -73.5f, 1000L);

        mvc.perform(get("/station/map"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStationMap_excludesStationWithNoInformation() throws Exception {
        insertStatus("s1", 3, 1000L);

        mvc.perform(get("/station/map"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getStationMap_returnsOnlyLatestPerStation() throws Exception {
        insertInformation("s1", "Station Old", 45.5f, -73.5f, 1000L);
        insertInformation("s1", "Station New", 45.6f, -73.6f, 2000L);
        insertStatus("s1", 1, 1000L);
        insertStatus("s1", 7, 2000L);

        mvc.perform(get("/station/map"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Station New"))
                .andExpect(jsonPath("$[0].num_bikes_available").value(7));
    }

    @Test
    void getStationMap_orderedByNameAsc() throws Exception {
        insertInformation("s2", "Beta",  45.6f, -73.6f, 1000L);
        insertInformation("s1", "Alpha", 45.5f, -73.5f, 1000L);
        insertStatus("s2", 2, 1000L);
        insertStatus("s1", 5, 1000L);

        mvc.perform(get("/station/map"))
                .andExpect(jsonPath("$[0].station_id").value("s1"))
                .andExpect(jsonPath("$[1].station_id").value("s2"));
    }

    private void insertInformation(String id, String name, float lat, float lon, long observedAt) {
        db.sql("""
                INSERT INTO station_information
                (station_id, name, lat, lon, observed_at)
                VALUES (:id, :name, :lat, :lon, :observedAt)""")
                .param("id", id).param("name", name)
                .param("lat", lat).param("lon", lon)
                .param("observedAt", observedAt)
                .update();
    }

    private void insertStatus(String id, int bikes, long observedAt) {
        db.sql("""
                INSERT INTO station_status
                (station_id, num_bikes_available, num_docks_available,
                 is_installed, is_renting, is_returning, last_reported, observed_at)
                VALUES (:id, :bikes, 10, 1, 1, 1, 0, :observedAt)""")
                .param("id", id).param("bikes", bikes)
                .param("observedAt", observedAt)
                .update();
    }
}
