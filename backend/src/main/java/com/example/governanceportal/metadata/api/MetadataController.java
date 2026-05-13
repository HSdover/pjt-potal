package com.example.governanceportal.metadata.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.metadata.dto.MetadataItem;
import com.example.governanceportal.metadata.dto.MetadataListRequest;
import com.example.governanceportal.metadata.service.MetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping
    List<MetadataItem> findAll() {
        return metadataService.findAll();
    }

    @PostMapping("/search")
    ListResponse<MetadataItem> search(@RequestBody MetadataListRequest request) {
        return metadataService.search(request);
    }
}
