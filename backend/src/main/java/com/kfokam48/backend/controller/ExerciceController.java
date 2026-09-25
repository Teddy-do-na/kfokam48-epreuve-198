package com.kfokam48.backend.controller;

import com.kfokam48.backend.dto.DeposerExerciceRequest;
import com.kfokam48.backend.dto.ExerciceResponse;
import com.kfokam48.backend.service.ExerciceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/exercices")
public class ExerciceController {
    private final ExerciceService exerciceService;

    public ExerciceController(ExerciceService exerciceService) {
        this.exerciceService = exerciceService;
    }

    @PostMapping
    public ResponseEntity<ExerciceResponse> deposer(@Valid @RequestBody DeposerExerciceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciceService.deposer(request));
    }

    @PutMapping("/{id}")
    public ExerciceResponse remplacer(@PathVariable @Positive Long id,
                                      @RequestParam @Positive Long etudiantId,
                                      @RequestBody RemplacerLienRequest request) {
        return exerciceService.remplacer(id, etudiantId, request.lien());
    }

    public record RemplacerLienRequest(@NotBlank @Size(max = 2048) String lien) {
    }
}
