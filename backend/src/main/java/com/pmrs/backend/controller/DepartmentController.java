package com.pmrs.backend.controller;

import com.pmrs.backend.entity.Department;
import com.pmrs.backend.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Department APIs", description = "Operations related to departments")
@RestController
@RequestMapping("/departments")
@CrossOrigin("*")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Get all departments")
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable Integer id) {
        return departmentService.getDepartmentById(id);
    }

    @Operation(summary = "Create a new department")
    @PostMapping
    public Department saveDepartment(@Valid @RequestBody Department department) {
        return departmentService.saveDepartment(department);
    }

    @Operation(summary = "Update department by ID")
    @PutMapping("/{id}")
    public Department updateDepartment(@PathVariable Integer id,
                                       @Valid @RequestBody Department department) {
        return departmentService.updateDepartment(id, department);
    }

    @Operation(summary = "Delete department by ID")
    @DeleteMapping("/{id}")
    public String deleteDepartment(@PathVariable Integer id) {
        departmentService.deleteDepartment(id);
        return "Department deleted successfully";
    }
}
