package com.example.governanceportal.lineage.api;

import com.example.governanceportal.lineage.dto.LineageFlowItem;
import com.example.governanceportal.lineage.service.LineageService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    private final LineageService lineageService;

    public LineageController(LineageService lineageService) {
        this.lineageService = lineageService;
    }

    @GetMapping
    List<LineageFlowItem> findAll() {
        return lineageService.findAll();
    }
}
