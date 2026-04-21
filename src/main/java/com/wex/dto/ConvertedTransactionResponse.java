package com.wex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertedTransactionResponse {
    private UUID identifier;
    private String description;
    private LocalDate transactionDate;
    private BigDecimal originalUsDollarAmount;
    private BigDecimal exchangeRateUsed;
    private BigDecimal convertedAmount;
}
