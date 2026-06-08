package com.example.governanceportal.metadata.integrated.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaDetail;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaItem;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaListRequest;
import com.example.governanceportal.metadata.integrated.service.IntegratedMetaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metadata/integrated")
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'META_VIEW')")
public class IntegratedMetaController {

    private final IntegratedMetaService integratedMetaService;

    public IntegratedMetaController(IntegratedMetaService integratedMetaService) {
        this.integratedMetaService = integratedMetaService;
    }

    @PostMapping("/search")
    public ListResponse<IntegratedMetaItem> search(@RequestBody IntegratedMetaListRequest request) {
        return integratedMetaService.search(request);
    }

    @GetMapping("/{metaId}")
    public IntegratedMetaDetail findById(@PathVariable String metaId) {
        return integratedMetaService.findById(metaId);
    }
}
