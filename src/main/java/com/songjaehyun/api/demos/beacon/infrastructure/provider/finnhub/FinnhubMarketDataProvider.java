package com.songjaehyun.api.demos.beacon.infrastructure.provider.finnhub;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.songjaehyun.api.demos.beacon.domain.MarketDataProvider;
import com.songjaehyun.api.demos.beacon.domain.model.Quote;
import com.songjaehyun.api.demos.beacon.domain.model.Symbol;

@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private final RestTemplate restTemplate;
    private final FinnhubProperties properties;

    public FinnhubMarketDataProvider(RestTemplate restTemplate, FinnhubProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public Quote getQuote(Symbol symbol) {
        String url = UriComponentsBuilder
            .fromUriString(properties.baseUrl())
            .path("/quote")
            .queryParam("symbol", symbol.value())
            .queryParam("token", properties.apiKey())
            .toUriString();

        FinnhubQuoteDto dto = restTemplate.getForObject(url, FinnhubQuoteDto.class);

        if (dto == null) {
            throw new IllegalStateException("Finnhub returned no data for symbol: " + symbol.value());
        }

        return new Quote(
            symbol,
            dto.current(),
            dto.high(),
            dto.low(),
            dto.open(),
            dto.prevclose(),
            Instant.now().toEpochMilli(),
            "FINNHUB"
        );
    }
}