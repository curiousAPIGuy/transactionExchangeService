package com.wex.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.dto.ConvertedTransactionResponse;
import com.wex.dto.PurchaseTransactionRequest;
import com.wex.entity.PurchaseTransaction;
import com.wex.exception.ConversionRateUnavailableException;
import com.wex.exception.GlobalExceptionHandler;
import com.wex.exception.TransactionNotFoundException;
import com.wex.service.PurchaseTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseTransactionController.class)
@Import(GlobalExceptionHandler.class)
public class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseTransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTransaction() throws Exception {
        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test transaction")
                .transactionDate(LocalDate.of(2023, 10, 10))
                .amount(BigDecimal.valueOf(100.50))
                .build();

        UUID id = UUID.randomUUID();
        PurchaseTransaction savedTransaction = PurchaseTransaction.builder()
                .id(id)
                .description("Test transaction")
                .transactionDate(LocalDate.of(2023, 10, 10))
                .amount(BigDecimal.valueOf(100.50))
                .build();

        when(service.saveTransaction(any(PurchaseTransactionRequest.class))).thenReturn(savedTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.amount").value(100.5));
    }

    @Test
    void testGetConvertedTransaction() throws Exception {
        UUID id = UUID.randomUUID();
        ConvertedTransactionResponse response = ConvertedTransactionResponse.builder()
                .identifier(id)
                .description("Test transaction")
                .transactionDate(LocalDate.of(2023, 10, 10))
                .originalUsDollarAmount(BigDecimal.valueOf(100.50))
                .exchangeRateUsed(BigDecimal.valueOf(0.85))
                .convertedAmount(BigDecimal.valueOf(85.43))
                .build();

        when(service.getConvertedTransaction(eq(id), eq("Euro"))).thenReturn(response);

        mockMvc.perform(get("/api/transactions/{id}", id)
                .param("currency", "Euro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifier").value(id.toString()))
                .andExpect(jsonPath("$.convertedAmount").value(85.43));
    }

    @Test
    void testCreateTransaction_MissingTransactionDate_ReturnsBadRequest() throws Exception {
        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test transaction")
                // Missing transactionDate
                .amount(BigDecimal.valueOf(100.50))
                .build();

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_MissingAmount_ReturnsBadRequest() throws Exception {
        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test transaction")
                .transactionDate(LocalDate.of(2023, 10, 10))
                // Missing amount
                .build();

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_NegativeAmount_ReturnsBadRequest() throws Exception {
        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test transaction")
                .transactionDate(LocalDate.of(2023, 10, 10))
                .amount(BigDecimal.valueOf(-10.50)) // Negative amount
                .build();

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetConvertedTransaction_NotFoundException_ReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getConvertedTransaction(eq(id), eq("Euro")))
                .thenThrow(new TransactionNotFoundException("Transaction not found"));

        mockMvc.perform(get("/api/transactions/{id}", id)
                .param("currency", "Euro"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetConvertedTransaction_ConversionRateUnavailable_ReturnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getConvertedTransaction(eq(id), eq("Euro")))
                .thenThrow(new ConversionRateUnavailableException("Rate not available"));

        mockMvc.perform(get("/api/transactions/{id}", id)
                .param("currency", "Euro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
