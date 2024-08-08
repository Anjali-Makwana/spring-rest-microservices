package com.SpringRestMicroservices.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import com.SpringRestMicroservices.entity.FxRate;
import com.SpringRestMicroservices.exception.CustomNullPointerException;
import com.SpringRestMicroservices.repository.FxRateRepository;
import com.SpringRestMicroservices.response.LatestExchangeRatesResponse;


@Service
public class FxRateService {
	
	private final RestTemplate restTemplate;
	private final FxRateRepository fxRateRepository;

	@Autowired
	public FxRateService(RestTemplate restTemplate, FxRateRepository fxRateRepository) {
		this.restTemplate = restTemplate;
		this.fxRateRepository = fxRateRepository;
	}
 
    private static final Logger logger = LoggerFactory.getLogger(FxRateService.class);
    
    @Value("${latest.exchange.rates.api.url}")
    private String latestExchangeRatesApiUrl;
    
    private final String API_URL = "https://api.frankfurter.app/latest";

    @Transactional
    public List<FxRate> getExchangeRate(String targetCurrency) {
    	logger.info("Fetching exchange rate for target currency: {}", targetCurrency);
    	
    	String[] tragetCurrArry = targetCurrency.split(",");
    	List<FxRate> fxRateList = new ArrayList<FxRate>();
    	for (String targetCurr : tragetCurrArry) {
    		 Optional<FxRate> existingExchangeRate = fxRateRepository.findFirstByTargetCurrencyOrderByDateDesc(targetCurr);
    	        if (existingExchangeRate.isPresent()) {
    	        	fxRateList.add(existingExchangeRate.get());
    	        } else {
    	        	logger.info("No rates found in database. Fetching from external API.");
    	        	fxRateList = fetchExchangeRateFromExternalAPI(targetCurrency);
    	        }
		}
		return fxRateList;
       
    }

    private List<FxRate> fetchExchangeRateFromExternalAPI(String targetCurrency) {
    	logger.info("Fetching rate from external API for target currency: {}", targetCurrency);
    	
    	String url = targetCurrency.isBlank() ? API_URL : API_URL + "?from=USD&to=" + targetCurrency;
    	logger.info("url for fetching response from external api : {}", url);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        	url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        List<FxRate> fxList = new ArrayList<FxRate>();
        if (response != null && response.getBody() != null) {
        	 Map<String, Object> mapResponse = response.getBody();
             Map<String, Double> rates = (Map<String, Double>) mapResponse.get("rates");
             
             String dateStr = (String) mapResponse.get("date");
             LocalDate date = LocalDate.parse(dateStr);
             for (Map.Entry<String, Double> entry : rates.entrySet()) {
                 String tCurrency = entry.getKey();
                 Double rate =((Number) entry.getValue()).doubleValue();

                 FxRate fxRates = fxRateRepository.findByDateAndTargetCurrency(date, tCurrency);
                 if (fxRates != null) {
                	 fxList.add(fxRates);
                 } else {
                     fxRates = new FxRate();
                	 fxRates.setDate(date);
                	 fxRates.setSourceCurrency("USD");
                	 fxRates.setTargetCurrency(tCurrency);
                	 fxRates.setRate(rate);
                	 fxRateRepository.save(fxRates);
                	 fxList.add(fxRates);
                 }
             }
             return fxList;
        }else {
        	 throw new CustomNullPointerException("Response not found for currency: " + targetCurrency);
        }
       
    }
    
    public List<FxRate> getLatestExchangeRates(String targetCurrency) {
    	logger.info("Fetching latest rates for target currency: {} in getLatestExchangeRates()", targetCurrency);
        List<FxRate> rates = fxRateRepository.findTop3BySourceCurrencyAndTargetCurrencyOrderByDateDesc("USD", targetCurrency);

        if (rates.size() < 3) {	
        	LocalDate endDate = LocalDate.now();
        	LocalDate startDate = endDate.minusDays(2);
        	
        	logger.info("Fetching latest rates for target currency: {} with start date : {} and end date : {}", targetCurrency, startDate, endDate);
           
        	String url = String.format("%s/%s..%s?from=USD&to=%s", latestExchangeRatesApiUrl, startDate, endDate, targetCurrency);
           
            /*HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String responses = restTemplate.getForObject(url, String.class);*/
	        try {
	        	LatestExchangeRatesResponse response = restTemplate.getForObject(url, LatestExchangeRatesResponse.class);	
	        	 if (response != null && response.getRates() != null) {
	                 List<FxRate> newRates = response.getRates().entrySet().stream().map(entry -> {
	                 	FxRate rate = new FxRate();
	                     rate.setSourceCurrency(response.getSourceCurrency());
	                     rate.setTargetCurrency(targetCurrency);
	                     rate.setRate(entry.getValue().get(targetCurrency));
	                     return rate;
	                 }).collect(Collectors.toList());

	                 fxRateRepository.saveAll(newRates);
	                 rates = newRates;
	             }
	        }
	        catch (UnknownContentTypeException e) {
                System.err.println("Unknown content type: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error fetching exchange rates: " + e.getMessage());
            }
           
        }
        return rates;
    }

}