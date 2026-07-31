package com.pmrs.backend.repository;

import com.pmrs.backend.entity.ExternalHrContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalHrContactRepository extends JpaRepository<ExternalHrContact, Integer> {

    List<ExternalHrContact> findByCompanyNameContainingIgnoreCase(String companyName);
}
