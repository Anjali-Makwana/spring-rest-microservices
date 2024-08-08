package com.SpringRestMicroservices;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.SpringRestMicroservices.entity.FxRate;
import com.SpringRestMicroservices.repository.FxRateRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestMicroservicesApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FxRateRepository fxRateRepository;
    
    private static final String sourceCurrency = "USD";
    private static final String targetCurrency = "EUR";

    @Test
    public void testGetRates() throws Exception {
        FxRate fxRate = new FxRate();
        fxRate.setDate(LocalDate.now());
        fxRate.setSourceCurrency(sourceCurrency);
        fxRate.setTargetCurrency(targetCurrency);
        fxRate.setRate(0.85);
        fxRateRepository.save(fxRate);

        mockMvc.perform(get("/fx?targetCurrency=EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value(sourceCurrency))
                .andExpect(jsonPath("$.targetCurrency").value(targetCurrency))
                .andExpect(jsonPath("$.rate").value(0.85));
    }

    @Test
    public void testGetLatestRates() throws Exception {
        FxRate fxRate1 = new FxRate();
        fxRate1.setDate(LocalDate.now().minusDays(1));
        fxRate1.setSourceCurrency(sourceCurrency);
        fxRate1.setTargetCurrency(targetCurrency);
        fxRate1.setRate(0.85);

        FxRate fxRate2 = new FxRate();
        fxRate2.setDate(LocalDate.now());
        fxRate2.setSourceCurrency(sourceCurrency);
        fxRate2.setTargetCurrency(targetCurrency);
        fxRate2.setRate(0.86);

        fxRateRepository.saveAll(Arrays.asList(fxRate1, fxRate2));

        mockMvc.perform(get("/fx/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rate").value(0.85))
                .andExpect(jsonPath("$[1].rate").value(0.86));
    }
}
