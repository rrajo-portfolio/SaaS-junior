package com.fiscalsaas.backend.verifactu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSifRecordRequest(@NotBlank @Size(max = 36) String invoiceId) {
}
