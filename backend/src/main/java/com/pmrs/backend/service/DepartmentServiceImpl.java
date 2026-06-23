package com.pmrs.backend.service;

import com.pmrs.backend.entity.Department;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Integer id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Integer id, Department updated) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        existing.setDeptName(updated.getDeptName());
        existing.setProgram(updated.getProgram());
        existing.setBranch(updated.getBranch());
        existing.setSection(updated.getSection());
        return departmentRepository.save(existing);
    }

    @Override
    public void deleteDepartment(Integer id) {
        departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        departmentRepository.deleteById(id);
    }
}
