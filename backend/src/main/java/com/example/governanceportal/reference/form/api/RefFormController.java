package com.example.governanceportal.reference.form.api;

import com.example.governanceportal.reference.form.dto.RefFormItem;
import com.example.governanceportal.reference.form.dto.RefFormSaveRequest;
import com.example.governanceportal.reference.form.service.RefFormService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference/forms")
public class RefFormController {

    private final RefFormService refFormService;

    public RefFormController(RefFormService refFormService) {
        this.refFormService = refFormService;
    }

    @GetMapping
    public List<RefFormItem> findAll() {
        return refFormService.findAll();
    }

    @GetMapping("/{id}")
    public RefFormItem findById(@PathVariable Long id) {
        return refFormService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefFormItem create(@Valid @RequestBody RefFormSaveRequest request) {
        return refFormService.create(request);
    }

    @PutMapping("/{id}")
    public RefFormItem update(@PathVariable Long id, @Valid @RequestBody RefFormSaveRequest request) {
        return refFormService.update(id, request);
    }
}
