package com.kfokam48.backend.controller;

import com.kfokam48.backend.dto.TableauEtudiantResponse;
import com.kfokam48.backend.service.TableauService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tableau")
public class TableauController {
    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping
    public List<TableauEtudiantResponse> consulter(@RequestParam Long promotionId) {
        return tableauService.consulter(promotionId);
    }
}
