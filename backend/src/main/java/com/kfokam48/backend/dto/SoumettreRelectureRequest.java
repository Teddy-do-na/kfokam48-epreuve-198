package com.kfokam48.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SoumettreRelectureRequest(
        @NotNull @Positive Long etudiantId,
        @NotNull @Min(0) @Max(20) Integer note,
        @NotBlank @Size(max = 4000) String commentaire
) {
}
