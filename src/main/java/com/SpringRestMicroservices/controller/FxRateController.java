package com.SpringRestMicroservices.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SpringRestMicroservices.entity.FxRate;
import com.SpringRestMicroservices.service.FxRateService;

@RestController
@RequestMapping("/fx")
public class FxRateController {

    @Autowired
    private FxRateService fxRateService;

    @GetMapping
    public FxRate getExchangeRate(@RequestParam(required = false, defaultValue = "EUR") String targetCurrency) {
        return fxRateService.getExchangeRate(targetCurrency);
    }

    @GetMapping("/{targetCurrency}")
    public List<FxRate> getLatestExchangeRates(@PathVariable String targetCurrency) {
        return fxRateService.getLatestExchangeRates(targetCurrency);
    }
}

