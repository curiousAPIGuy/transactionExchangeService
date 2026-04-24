package com.wex.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.wex.dto.TreasuryApiResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import com.wex.exception.ConversionRateUnavailableException;

@Component
public class TreasuryApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TreasuryApiClient.class);

    private final RestTemplate restTemplate;

    @Value("${treasury.api.url:https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange}")
    private String treasuryApiUrl;

    public TreasuryApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TreasuryApiResponse getExchangeRates(String currency, LocalDate purchaseDate) {
        LocalDate sixMonthsAgo = purchaseDate.minusMonths(6);

        String url = UriComponentsBuilder.fromHttpUrl(treasuryApiUrl)
                .queryParam("fields", "record_date,exchange_rate,country_currency_desc")
                .queryParam("filter", String.format("country_currency_desc:eq:%s,record_date:lte:%s,record_date:gte:%s",
                        currency, purchaseDate.toString(), sixMonthsAgo.toString()))
                .queryParam("sort", "-record_date")
                .queryParam("page[size]", 1)
                .build(false) // don't encode here because RestTemplate encodes, and we need precise eq
                              // handling, wait, better use build()
                .toUriString();

        try {
            return restTemplate.getForObject(url, TreasuryApiResponse.class);
        } catch (RestClientException ex) {
            logger.error("Error communicating with Treasury API: {}", ex.getMessage());
            throw new ConversionRateUnavailableException(
                    "Treasury API is currently unavailable or not responding. Please try again later.");
        }
    }
}
