package com.songjaehyun.api.demos.beacon.application;

import org.springframework.stereotype.Service;

import com.songjaehyun.api.demos.beacon.domain.MarketDataProvider;
import com.songjaehyun.api.demos.beacon.domain.model.Quote;
import com.songjaehyun.api.demos.beacon.domain.model.Symbol;

@Service
public class GetQuoteService {

    private final MarketDataProvider marketDataProvider;

    public GetQuoteService(MarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    public Quote execute(String symbolValue) {
        Symbol symbol = new Symbol(symbolValue);
    return marketDataProvider.getQuote(symbol);
    }
}