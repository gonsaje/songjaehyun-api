package com.songjaehyun.api.demos.beacon.infrastructure.provider.finnhub;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "beacon.providers.finnhub")
public record FinnhubProperties(
    String baseUrl,
    String apiKey
) {}