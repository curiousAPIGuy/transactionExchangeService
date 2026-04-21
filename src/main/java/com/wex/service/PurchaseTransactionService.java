package com.wex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.wex.client.TreasuryApiClient;
import com.wex.dto.ConvertedTransactionResponse;
import com.wex.dto.PurchaseTransactionRequest;
import com.wex.dto.TreasuryApiResponse;
import com.wex.entity.PurchaseTransaction;
import com.wex.exception.ConversionRateUnavailableException;
import com.wex.exception.TransactionNotFoundException;
import com.wex.repository.PurchaseTransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository repository;
    private final TreasuryApiClient treasuryApiClient;

    public PurchaseTransaction saveTransaction(PurchaseTransactionRequest request) {
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .build();
        return repository.save(transaction);
    }

    public ConvertedTransactionResponse getConvertedTransaction(UUID id, String targetCurrency) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Purchase transaction with id " + id + " not found."));

        TreasuryApiResponse apiResponse = treasuryApiClient.getExchangeRates(targetCurrency, transaction.getTransactionDate());

        if (apiResponse == null || apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
            throw new ConversionRateUnavailableException(
                    "No currency conversion rate is available within 6 months equal to or before the purchase date for target currency: " + targetCurrency);
        }

        TreasuryApiResponse.TreasuryRate rate = apiResponse.getData().get(0);
        BigDecimal exchangeRate = new BigDecimal(rate.getExchange_rate());
        BigDecimal convertedAmount = transaction.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        return ConvertedTransactionResponse.builder()
                .identifier(transaction.getId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .originalUsDollarAmount(transaction.getAmount())
                .exchangeRateUsed(exchangeRate)
                .convertedAmount(convertedAmount)
                .build();
    }
}
