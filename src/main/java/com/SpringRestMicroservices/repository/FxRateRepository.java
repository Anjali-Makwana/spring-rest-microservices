package com.SpringRestMicroservices.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SpringRestMicroservices.entity.FxRate;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, Long> {

	//List<FxRate> findTop3BySourceCurrencyAndTargetCurrencyOrderByDateDesc(String sourceCurrency, String targetCurrency);
	List<FxRate> findByTargetCurrencyOrderByDateDesc(String targetCurrency, Pageable pageable);

	Optional<FxRate> findFirstByTargetCurrencyOrderByDateDesc(String targetCurrency);
	//Optional<FxRate> findByDateAndSourceCurrencyAndTargetCurrency(LocalDate date, String sourceCurrency, String targetCurrency);
}
