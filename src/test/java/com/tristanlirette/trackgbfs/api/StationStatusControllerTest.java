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
class StationStatusControllerTest {

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
    void getStationStatus_returnsEmptyArrayWhenNoData() throws Exception {
        mvc.perform(get("/station/status"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getStationStatus_returnsLatestStatus() throws Exception {
        insertStatus("s1", 5, 15, 1000L);

        mvc.perform(get("/station/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].station_id").value("s1"))
                .andExpect(jsonPath("$[0].num_bikes_available").value(5))
                .andExpect(jsonPath("$[0].num_docks_available").value(15))
                .andExpect(jsonPath("$[0].is_installed").value(true));
    }

    @Test
    void getStationStatus_returnsOnlyLatestStatusPerStation() throws Exception {
        insertStatus("s1", 3, 17, 1000L);
        insertStatus("s1", 7, 13, 2000L);

        mvc.perform(get("/station/status"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].num_bikes_available").value(7));
    }

    @Test
    void getStationStatus_returnsLatestStatusForAllStations() throws Exception {
        insertStatus("s1", 3, 17, 1000L);
        insertStatus("s2", 5, 15, 2000L);

        mvc.perform(get("/station/status"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getStationStatus_orderedByLastReportedDesc() throws Exception {
        insertStatus("s1", 4, 6, 1000L, 1000L);
        insertStatus("s2", 2, 8, 1000L, 2000L);

        mvc.perform(get("/station/status"))
                .andExpect(jsonPath("$[0].station_id").value("s2"))
                .andExpect(jsonPath("$[1].station_id").value("s1"));
    }

    private void insertStatus(String id, int bikes, int docks, long observedAt) {
        insertStatus(id, bikes, docks, observedAt, 0L);
    }

    private void insertStatus(String id, int bikes, int docks, long observedAt, long lastReported) {
        db.sql("""
                INSERT INTO station_status
                (station_id, num_bikes_available, num_docks_available,
                 is_installed, is_renting, is_returning, last_reported, observed_at)
                VALUES (:id, :bikes, :docks, 1, 1, 1, :lastReported, :observedAt)""")
                .param("id", id).param("bikes", bikes).param("docks", docks)
                .param("lastReported", lastReported).param("observedAt", observedAt)
                .update();
    }
}
