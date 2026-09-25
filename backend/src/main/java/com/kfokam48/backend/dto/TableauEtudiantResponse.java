package com.kfokam48.backend.dto;

public record TableauEtudiantResponse(Long etudiantId, String nom, Long presences,
                                      Long exercicesDeposes, Double moyenne, Long relecturesEnAttente) {
}
