package com.songjaehyun.api.demos.beacon.infrastructure.provider.finnhub;

public record FinnhubQuoteDto(
    double current,   // current price
    double high,   // high price of the day
    double low,   // low price of the day
    double open,   // open price of the day
    double prevclose,  // previous close price
    long t      // timestamp
) {}