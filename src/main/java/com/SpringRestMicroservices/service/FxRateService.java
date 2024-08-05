package com.SpringRestMicroservices.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.SpringRestMicroservices.entity.FxRate;
import com.SpringRestMicroservices.repository.FxRateRepository;

@Service
public class FxRateService {

    @Autowired
    private FxRateRepository fxRateRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    private static final Logger logger = LoggerFactory.getLogger(FxRateService.class);
    
    private final String API_URL_LATEST = "https://api.frankfurter.app/latest";
    //private final String API_URL_DATE = "https://api.frankfurter.app/";

    public FxRate getExchangeRate(String targetCurrency) {
    	logger.info("Fetching exchange rate for target currency: {}", targetCurrency);
        Optional<FxRate> existingExchangeRate = fxRateRepository.findFirstByTargetCurrencyOrderByDateDesc(targetCurrency);
        if (existingExchangeRate.isPresent()) {
            return existingExchangeRate.get();
        } else {
        	logger.info("No rates found in database. Fetching from external API.");
            FxRate fxRate = fetchExchangeRateFromExternalAPI(targetCurrency);
            fxRateRepository.save(fxRate);
            return fxRate;
        }
    }

    private FxRate fetchExchangeRateFromExternalAPI(String targetCurrency) {
    	
    	logger.info("Fetching rate from external API for target currency: {}", targetCurrency);
        
        //ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(apiUrl, Map.class);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        	API_URL_LATEST,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response != null && response.getBody() != null) {
        	 Map<String, Object> body = response.getBody();
             Map<String, Double> rates = (Map<String, Double>) body.get("rates");
             
             FxRate fxRate = new FxRate();
             fxRate.setDate(LocalDate.now());
             fxRate.setSourceCurrency("USD");
             fxRate.setTargetCurrency(targetCurrency);
             
             if(targetCurrency.equalsIgnoreCase("EUR")) {
             	
             	Object value = body.get("amount");

             	if (value instanceof BigDecimal) {
                     BigDecimal bigDecimalValue = (BigDecimal) value;
                     fxRate.setRate(bigDecimalValue);
                 } else if (value instanceof Number) {
                     BigDecimal bigDecimalValue = new BigDecimal(((Number) value).doubleValue());
                     fxRate.setRate(bigDecimalValue);
                 } else {
                     System.out.println("Value is not a BigDecimal, Number, or is null.");
                 }
             }else {
             	fxRate.setRate(BigDecimal.valueOf(rates.get(targetCurrency)));
             }
             return fxRate;
        }else {
        	return null;
        }
       
    }

    public List<FxRate> getLatestExchangeRates(String targetCurrency) {
    	logger.debug("Fetching latest rates for target currency: {}", targetCurrency);
        Pageable pageable = PageRequest.of(0, 3);
        return fxRateRepository.findByTargetCurrencyOrderByDateDesc(targetCurrency, pageable);
    }
}