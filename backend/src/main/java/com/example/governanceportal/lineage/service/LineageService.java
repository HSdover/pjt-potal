package com.example.governanceportal.lineage.service;

import com.example.governanceportal.lineage.dto.LineageFlowItem;
import com.example.governanceportal.lineage.repository.LineageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LineageService {

    private final LineageRepository lineageRepository;

    public LineageService(LineageRepository lineageRepository) {
        this.lineageRepository = lineageRepository;
    }

    public List<LineageFlowItem> findAll() {
        return lineageRepository.findAll();
    }
}
