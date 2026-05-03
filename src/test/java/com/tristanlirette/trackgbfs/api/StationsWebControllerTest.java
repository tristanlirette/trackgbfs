package com.tristanlirette.trackgbfs.api;

import com.tristanlirette.trackgbfs.gbfs.GbfsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class StationsWebControllerTest {

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
    void rootReturnsHtml() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void rootShowsEmptyTableWhenNoData() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//tbody/tr").nodeCount(0));
    }

    @Test
    void rootShowsOneRowPerStation() throws Exception {
        insertStatus("s1", 5, 15, 1735000000L, 1735000100L);
        insertStatus("s2", 2, 8,  1735000200L, 1735000300L);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//tbody/tr").nodeCount(2));
    }

    @Test
    void rootShowsLatestStatusOnlyPerStation() throws Exception {
        insertStatus("s1", 3, 17, 1735000000L, 1735000100L);
        insertStatus("s1", 7, 13, 1735000200L, 1735000300L);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//tbody/tr").nodeCount(1))
                .andExpect(xpath("//tbody/tr/td[2]").string("7"));
    }

    @Test
    void rootShowsStationIdInTable() throws Exception {
        insertStatus("STATION_A", 5, 15, 1735000000L, 1735000100L);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//tbody/tr/td[1]").string("STATION_A"));
    }

    private void insertStatus(String id, int bikes, int docks, long lastReported, long observedAt) {
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
