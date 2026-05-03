package com.fiscalsaas.backend.verifactu;

public record SifHashChainVerificationResponse(boolean valid, int recordCount, String lastHash) {
}
