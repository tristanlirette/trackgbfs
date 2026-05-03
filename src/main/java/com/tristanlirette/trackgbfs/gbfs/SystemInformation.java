package com.tristanlirette.trackgbfs.gbfs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SystemInformation(
        @JsonProperty("system_id") String systemId,
        String language,
        String name,
        @JsonProperty("short_name") String shortName,
        String operator,
        String url,
        @JsonProperty("purchase_url") String purchaseUrl,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("phone_number") String phoneNumber,
        String email,
        @JsonProperty("feed_contact_email") String feedContactEmail,
        String timezone,
        @JsonProperty("license_url") String licenseUrl) {
}
