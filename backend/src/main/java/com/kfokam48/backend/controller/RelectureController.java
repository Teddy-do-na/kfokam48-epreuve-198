package com.kfokam48.backend.controller;

import com.kfokam48.backend.dto.RelectureAssigneeResponse;
import com.kfokam48.backend.dto.RelectureEtudiantResponse;
import com.kfokam48.backend.dto.SoumettreRelectureRequest;
import java.util.List;
import com.kfokam48.backend.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relectures")
public class RelectureController {
    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    @GetMapping
    public List<RelectureEtudiantResponse> consulter(@RequestParam Long etudiantId) {
        return relectureService.consulterParEtudiant(etudiantId);
    }

    @GetMapping("/assignees")
    public List<RelectureAssigneeResponse> consulterAssignees(@RequestParam Long etudiantId) {
        return relectureService.consulterAssignees(etudiantId);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> soumettre(@PathVariable Long id, @Valid @RequestBody SoumettreRelectureRequest request) {
        relectureService.soumettre(id, request);
        return ResponseEntity.ok().build();
    }
}
