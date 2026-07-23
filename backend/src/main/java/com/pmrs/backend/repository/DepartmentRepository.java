package com.pmrs.backend.repository;

import com.pmrs.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    /**
     * A branch+program maps to several Department rows — one per section
     * (e.g. CSE M.Tech → sections M13 and M14) — so this returns a list,
     * ordered so callers can deterministically pick the first section.
     */
    List<Department> findByBranchAndProgramOrderByDeptIdAsc(String branch, String program);
}