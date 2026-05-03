package com.fiscalsaas.backend.verifactu;

import jakarta.validation.constraints.Size;

public record CreateAeatTransmissionRequest(@Size(max = 30) String mode) {
}
