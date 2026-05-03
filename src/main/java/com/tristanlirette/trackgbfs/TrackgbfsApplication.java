package com.tristanlirette.trackgbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsRuntimeHints;
import com.tristanlirette.trackgbfs.gbfs.GbfsResponse;
import com.tristanlirette.trackgbfs.gbfs.StationInformation;
import com.tristanlirette.trackgbfs.gbfs.StationStatus;
import com.tristanlirette.trackgbfs.gbfs.SystemInformation;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ConfigurationPropertiesScan
@ImportRuntimeHints(TrackGbfsRuntimeHints.class)
@RegisterReflectionForBinding({
		GbfsResponse.class,
		SystemInformation.class,
		StationInformation.class,
		StationInformation.Station.class,
		StationStatus.class,
		StationStatus.Entry.class,
})
public class TrackgbfsApplication {
	public static void main(String[] args) {
		SpringApplication.run(TrackgbfsApplication.class, args);
	}
}