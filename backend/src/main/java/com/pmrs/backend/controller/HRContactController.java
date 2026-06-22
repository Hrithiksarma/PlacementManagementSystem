package com.pmrs.backend.controller;

import com.pmrs.backend.entity.HRContact;
import com.pmrs.backend.service.HRContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HR Contact APIs", description = "Operations related to HR contacts")
@RestController
@RequestMapping("/hrcontacts")
@CrossOrigin("*")
public class HRContactController {

    private final HRContactService hrContactService;

    public HRContactController(HRContactService hrContactService) {
        this.hrContactService = hrContactService;
    }

    @Operation(summary = "Get all HR contacts")
    @GetMapping
    public List<HRContact> getAllHRContacts() {
        return hrContactService.getAllHRContacts();
    }

    @Operation(summary = "Get HR contact by ID")
    @GetMapping("/{id}")
    public HRContact getHRContactById(@PathVariable Integer id) {
        return hrContactService.getHRContactById(id);
    }

    @Operation(summary = "Get HR contacts by company ID")
    @GetMapping("/company/{companyId}")
    public List<HRContact> getHRContactsByCompany(@PathVariable Integer companyId) {
        return hrContactService.getHRContactsByCompanyId(companyId);
    }

    @Operation(summary = "Create a new HR contact")
    @PostMapping
    public HRContact createHRContact(@Valid @RequestBody HRContact hrContact) {
        return hrContactService.saveHRContact(hrContact);
    }

    @Operation(summary = "Update HR contact by ID")
    @PutMapping("/{id}")
    public HRContact updateHRContact(@PathVariable Integer id,
                                     @Valid @RequestBody HRContact hrContact) {
        return hrContactService.updateHRContact(id, hrContact);
    }

    @Operation(summary = "Delete HR contact by ID")
    @DeleteMapping("/{id}")
    public String deleteHRContact(@PathVariable Integer id) {
        hrContactService.deleteHRContact(id);
        return "HR Contact deleted successfully";
    }
}
