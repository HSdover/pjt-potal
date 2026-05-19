package com.example.governanceportal.reference.approval.api;

import com.example.governanceportal.reference.approval.dto.RefApprovalDecisionRequest;
import com.example.governanceportal.reference.approval.dto.RefApprovalItem;
import com.example.governanceportal.reference.approval.service.RefApprovalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference/approvals")
public class RefApprovalController {

    private final RefApprovalService refApprovalService;

    public RefApprovalController(RefApprovalService refApprovalService) {
        this.refApprovalService = refApprovalService;
    }

    @GetMapping
    public List<RefApprovalItem> findAll() {
        return refApprovalService.findAll();
    }

    @GetMapping("/{id}")
    public RefApprovalItem findById(@PathVariable Long id) {
        return refApprovalService.findById(id);
    }

    @PostMapping("/{id}/approve")
    public RefApprovalItem approve(@PathVariable Long id, @Valid @RequestBody(required = false) RefApprovalDecisionRequest request) {
        return refApprovalService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    public RefApprovalItem reject(@PathVariable Long id, @Valid @RequestBody(required = false) RefApprovalDecisionRequest request) {
        return refApprovalService.reject(id, request);
    }
}
