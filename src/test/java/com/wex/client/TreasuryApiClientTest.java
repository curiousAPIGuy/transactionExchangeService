package com.wex.client;

import com.wex.dto.TreasuryApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.springframework.web.client.RestClientException;
import com.wex.exception.ConversionRateUnavailableException;

@ExtendWith(MockitoExtension.class)
public class TreasuryApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private TreasuryApiClient treasuryApiClient;

    @BeforeEach
    void setUp() {
        treasuryApiClient = new TreasuryApiClient(restTemplate);
        ReflectionTestUtils.setField(treasuryApiClient, "treasuryApiUrl", "https://api.test");
    }

    @Test
    void getExchangeRates_shouldCallRestTemplateWithCorrectUrl() {
        LocalDate purchaseDate = LocalDate.of(2023, 10, 15);

        String expectedUrl = "https://api.test?fields=record_date,exchange_rate,country_currency_desc" +
                "&filter=country_currency_desc:eq:Euro,record_date:lte:2023-10-15,record_date:gte:2023-04-15" +
                "&sort=-record_date&page[size]=1";

        TreasuryApiResponse mockResponse = new TreasuryApiResponse();
        when(restTemplate.getForObject(expectedUrl, TreasuryApiResponse.class)).thenReturn(mockResponse);

        TreasuryApiResponse response = treasuryApiClient.getExchangeRates("Euro", purchaseDate);

        assertEquals(mockResponse, response);
    }

    @Test
    void getExchangeRates_shouldThrowConversionRateUnavailableException_whenRestClientExceptionOccurs() {
        LocalDate purchaseDate = LocalDate.of(2023, 10, 15);

        String expectedUrl = "https://api.test?fields=record_date,exchange_rate,country_currency_desc" +
                "&filter=country_currency_desc:eq:Euro,record_date:lte:2023-10-15,record_date:gte:2023-04-15" +
                "&sort=-record_date&page[size]=1";

        when(restTemplate.getForObject(expectedUrl, TreasuryApiResponse.class))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThrows(ConversionRateUnavailableException.class, () -> {
            treasuryApiClient.getExchangeRates("Euro", purchaseDate);
        });
    }
}
