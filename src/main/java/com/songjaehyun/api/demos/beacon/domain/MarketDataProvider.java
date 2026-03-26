package com.songjaehyun.api.demos.beacon.domain;

import com.songjaehyun.api.demos.beacon.domain.model.Quote;
import com.songjaehyun.api.demos.beacon.domain.model.Symbol;

public interface MarketDataProvider {
    Quote getQuote(Symbol symbol);
}