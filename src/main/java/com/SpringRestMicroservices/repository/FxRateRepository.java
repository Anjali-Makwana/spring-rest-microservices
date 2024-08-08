package com.SpringRestMicroservices.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SpringRestMicroservices.entity.FxRate;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, Long> {

	List<FxRate> findTop3BySourceCurrencyAndTargetCurrencyOrderByDateDesc(String sourceCurrency, String targetCurrency);

	Optional<FxRate> findFirstByTargetCurrencyOrderByDateDesc(String targetCurrency);

	FxRate findByDateAndTargetCurrency(LocalDate date, String targetCurrency);
	
}
