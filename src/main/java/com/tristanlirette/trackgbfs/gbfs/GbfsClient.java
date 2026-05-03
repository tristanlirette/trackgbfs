package com.tristanlirette.trackgbfs.gbfs;

import com.tristanlirette.trackgbfs.config.TrackGbfsProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GbfsClient {

    private final RestClient restClient;
    private final String language;

    public GbfsClient(RestClient.Builder builder, TrackGbfsProperties properties) {
        this.restClient = builder.baseUrl(properties.feed().baseUrl()).build();
        this.language = properties.feed().language();
    }

    public <T> GbfsResponse<T> fetch(String endpoint, Class<T> payloadType) {
        ParameterizedTypeReference<GbfsResponse<T>> ref = ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(GbfsResponse.class, payloadType).getType());
        return restClient.get()
                .uri("/{language}/{endpoint}", language, endpoint)
                .retrieve()
                .body(ref);
    }
}
