package com.pmrs.backend.service;

import com.pmrs.backend.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> getAllDepartments();

    Department getDepartmentById(Integer id);

    Department saveDepartment(Department department);

    Department updateDepartment(Integer id, Department department);

    void deleteDepartment(Integer id);
}
