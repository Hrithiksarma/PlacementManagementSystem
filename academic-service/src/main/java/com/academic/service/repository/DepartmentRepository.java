// ── DepartmentRepository.java ────────────────────────────────
package com.academic.service.repository;

import com.academic.service.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
