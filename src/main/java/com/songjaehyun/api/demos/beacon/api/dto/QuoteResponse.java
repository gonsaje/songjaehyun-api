package com.songjaehyun.api.demos.beacon.api.dto;

import com.songjaehyun.api.demos.beacon.domain.model.Quote;

public record QuoteResponse(String symbolString, double currentPrice, double highPrice, double lowPrice, double openPrice, double previousClosePrice, long asOfEpochMillis, String source) {

    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
            quote.symbol().value(),
            quote.currentPrice(),
            quote.highPrice(),
            quote.lowPrice(),
            quote.openPrice(),
            quote.previousClosePrice(),
            quote.asOfEpochMillis(),
            quote.source()
        );
    }
}