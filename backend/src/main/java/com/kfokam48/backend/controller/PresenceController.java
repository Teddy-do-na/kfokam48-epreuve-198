package com.kfokam48.backend.controller;

import com.kfokam48.backend.dto.PresenceManuelleRequest;
import com.kfokam48.backend.dto.PresenceRequest;
import com.kfokam48.backend.dto.PresenceResponse;
import com.kfokam48.backend.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {
    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping
    public ResponseEntity<PresenceResponse> marquer(@Valid @RequestBody PresenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presenceService.marquer(request.code(), request.etudiantId()));
    }

    @PostMapping("/manuelle")
    public ResponseEntity<PresenceResponse> ajouterManuellement(@Valid @RequestBody PresenceManuelleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presenceService.ajouterManuellement(request.sessionId(), request.etudiantId()));
    }
}
