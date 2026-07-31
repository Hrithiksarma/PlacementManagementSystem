package com.pmrs.backend.controller;

import com.pmrs.backend.entity.ExternalHrContact;
import com.pmrs.backend.service.ExternalHrContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "External HR Contact APIs", description = "HR contacts for companies not tracked in PRMS")
@RestController
@RequestMapping("/external-hrcontacts")
@CrossOrigin("*")
public class ExternalHrContactController {

    private final ExternalHrContactService service;

    public ExternalHrContactController(ExternalHrContactService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new external HR contact")
    @PostMapping
    public ExternalHrContact create(@Valid @RequestBody ExternalHrContact contact) {
        return service.save(contact);
    }

    @Operation(summary = "Update an external HR contact by ID")
    @PutMapping("/{id}")
    public ExternalHrContact update(@PathVariable Integer id, @Valid @RequestBody ExternalHrContact contact) {
        return service.update(id, contact);
    }

    @Operation(summary = "Delete an external HR contact by ID")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "External HR contact deleted successfully";
    }
}
