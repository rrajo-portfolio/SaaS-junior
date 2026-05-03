package com.fiscalsaas.backend.verifactu;

import jakarta.validation.constraints.Size;

public record CancelSifRecordRequest(@Size(max = 500) String reason) {
}
