package com.songjaehyun.api.demos.beacon.domain.model;

public class Quote {
    private final Symbol symbol;
    private final double price;
    private final long asOf; 
    private final String source;

    Quote(Symbol symbol, double price, long asOf, String source) {
        this.symbol = symbol;
        this.price = price;
        this.asOf = asOf;
        this.source = source;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public long getAsOf() {
        return asOf;
    }

    public String getSource() {
        return source;
    }
}