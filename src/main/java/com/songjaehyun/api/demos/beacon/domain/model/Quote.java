package com.songjaehyun.api.demos.beacon.domain.model;

public record Quote(
    Symbol symbol,
    double currentPrice,
    double highPrice,
    double lowPrice,
    double openPrice,
    double previousClosePrice,
    long asOfEpochMillis,
    String source
) {}