package com.SpringRestMicroservices.response;

import java.util.Map;

import lombok.Data;

@Data
public class LatestExchangeRatesResponse {

    private String sourceCurrency;
    private String targetCurrency;
    private Map<String, Map<String, Double>> rates;

}
