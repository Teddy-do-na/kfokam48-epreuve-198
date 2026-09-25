package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OuvrirSessionRequest(
        @NotBlank String titre,
        @NotNull @Positive Long promotionId
) {
}
