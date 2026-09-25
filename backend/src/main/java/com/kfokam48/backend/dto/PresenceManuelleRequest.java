package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PresenceManuelleRequest(
        @NotNull @Positive Long sessionId,
        @NotNull @Positive Long etudiantId
) {
}
