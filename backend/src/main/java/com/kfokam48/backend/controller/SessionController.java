package com.kfokam48.backend.controller;

import com.kfokam48.backend.dto.OuvrirSessionRequest;
import com.kfokam48.backend.dto.SessionClotureResponse;
import com.kfokam48.backend.dto.SessionResponse;
import com.kfokam48.backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> ouvrir(@Valid @RequestBody OuvrirSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.ouvrir(request));
    }

    @PostMapping("/{id}/cloture")
    public ResponseEntity<SessionClotureResponse> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.cloturer(id));
    }
}
