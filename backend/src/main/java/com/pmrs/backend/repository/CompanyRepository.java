package com.pmrs.backend.repository;

import com.pmrs.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    List<Company> findByTier(String tier);

    List<Company> findBySector(String sector);
}
