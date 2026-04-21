package com.wex.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wex.dto.ConvertedTransactionResponse;
import com.wex.dto.PurchaseTransactionRequest;
import com.wex.entity.PurchaseTransaction;
import com.wex.service.PurchaseTransactionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class PurchaseTransactionController {

    private final PurchaseTransactionService service;

    @PostMapping
    public ResponseEntity<PurchaseTransaction> createTransaction(@Valid @RequestBody PurchaseTransactionRequest request) {
        PurchaseTransaction savedTransaction = service.saveTransaction(request);
        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvertedTransactionResponse> getConvertedTransaction(
            @PathVariable UUID id,
            @RequestParam String currency) {
        ConvertedTransactionResponse response = service.getConvertedTransaction(id, currency);
        return ResponseEntity.ok(response);
    }
}
