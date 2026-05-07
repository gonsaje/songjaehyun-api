package com.songjaehyun.api.demos.beacon.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.songjaehyun.api.demos.beacon.api.dto.QuoteResponse;
import com.songjaehyun.api.demos.beacon.application.GetQuoteService;
import com.songjaehyun.api.demos.beacon.domain.model.Quote;

@RestController
@RequestMapping("/api/demos/beacon")
public class BeaconController {

    private final GetQuoteService getQuoteService;

    public BeaconController(GetQuoteService getQuoteService) {
        this.getQuoteService = getQuoteService;
    }

    @GetMapping("/quote")
    public QuoteResponse getQuote(@RequestParam String symbol) {
        Quote quote = getQuoteService.execute(symbol);
        return QuoteResponse.from(quote);
    }
}