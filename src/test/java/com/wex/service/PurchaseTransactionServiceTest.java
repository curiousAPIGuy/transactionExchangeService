package com.wex.service;

import com.wex.client.TreasuryApiClient;
import com.wex.dto.ConvertedTransactionResponse;
import com.wex.dto.PurchaseTransactionRequest;
import com.wex.dto.TreasuryApiResponse;
import com.wex.entity.PurchaseTransaction;
import com.wex.exception.ConversionRateUnavailableException;
import com.wex.exception.TransactionNotFoundException;
import com.wex.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @Mock
    private TreasuryApiClient treasuryApiClient;

    @InjectMocks
    private PurchaseTransactionService service;

    @Test
    void saveTransaction_shouldReturnSavedTransaction() {
        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test Description")
                .transactionDate(LocalDateTime.of(2023, 10, 15, 12, 0, 0))
                .amount(BigDecimal.valueOf(100.123))
                .build();

        PurchaseTransaction savedTransaction = PurchaseTransaction.builder()
                .id(UUID.randomUUID())
                .description("Test Description")
                .transactionDate(LocalDateTime.of(2023, 10, 15, 12, 0, 0))
                .amount(BigDecimal.valueOf(100.12))
                .build();

        when(repository.save(any(PurchaseTransaction.class))).thenReturn(savedTransaction);

        PurchaseTransaction result = service.saveTransaction(request);

        assertNotNull(result);
        assertEquals("Test Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(100.12), result.getAmount());
    }

    @Test
    void getConvertedTransaction_shouldReturnConvertedResponse() {
        UUID id = UUID.randomUUID();
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .id(id)
                .description("Test")
                .transactionDate(LocalDateTime.of(2023, 10, 15, 12, 0, 0))
                .amount(BigDecimal.valueOf(100.00))
                .build();

        TreasuryApiResponse apiResponse = new TreasuryApiResponse();
        TreasuryApiResponse.TreasuryRate rate = new TreasuryApiResponse.TreasuryRate();
        rate.setExchange_rate("0.85");
        apiResponse.setData(Collections.singletonList(rate));

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(treasuryApiClient.getExchangeRates("Euro", LocalDate.of(2023, 10, 15)))
                .thenReturn(apiResponse);

        ConvertedTransactionResponse result = service.getConvertedTransaction(id, "Euro");

        assertNotNull(result);
        assertEquals(id, result.getIdentifier());
        assertEquals(BigDecimal.valueOf(100.00), result.getOriginalUsDollarAmount());
        assertEquals(BigDecimal.valueOf(0.85), result.getExchangeRateUsed());
        assertEquals(BigDecimal.valueOf(85.00).setScale(2, RoundingMode.HALF_UP), result.getConvertedAmount());
    }

    @Test
    void getConvertedTransaction_shouldThrowTransactionNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> service.getConvertedTransaction(id, "Euro"));
    }

    @Test
    void getConvertedTransaction_shouldThrowConversionRateUnavailableException_whenApiResponseNull() {
        UUID id = UUID.randomUUID();
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .id(id)
                .description("Test")
                .transactionDate(LocalDateTime.of(2023, 10, 15, 12, 0, 0))
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(transaction));
        when(treasuryApiClient.getExchangeRates("Euro", LocalDate.of(2023, 10, 15))).thenReturn(null);

        assertThrows(ConversionRateUnavailableException.class, () -> service.getConvertedTransaction(id, "Euro"));
    }
}
