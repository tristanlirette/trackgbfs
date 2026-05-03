package com.tristanlirette.trackgbfs;

import com.tristanlirette.trackgbfs.gbfs.GbfsClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TrackgbfsApplicationTests {

	@MockitoBean
	GbfsClient gbfsClient;

	@Test
	void contextLoads() {
	}

}
