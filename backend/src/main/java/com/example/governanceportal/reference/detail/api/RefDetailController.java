package com.example.governanceportal.reference.detail.api;

import com.example.governanceportal.reference.detail.dto.RefDetailItem;
import com.example.governanceportal.reference.detail.service.RefDetailService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference/details")
public class RefDetailController {

    private final RefDetailService refDetailService;

    public RefDetailController(RefDetailService refDetailService) {
        this.refDetailService = refDetailService;
    }

    @GetMapping
    public List<RefDetailItem> findAll() {
        return refDetailService.findAll();
    }

    @GetMapping("/{id}")
    public RefDetailItem findById(@PathVariable Long id) {
        return refDetailService.findById(id);
    }
}
