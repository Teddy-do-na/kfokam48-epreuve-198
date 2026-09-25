package com.kfokam48.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record DeposerExerciceRequest(
        @NotNull @Positive Long sessionId,
        @NotNull @Positive Long etudiantId,
        @NotBlank @Size(max = 2048) String lien
) {
}
