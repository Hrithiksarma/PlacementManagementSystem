package com.pmrs.backend.repository;

import com.pmrs.backend.entity.HRContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HRContactRepository extends JpaRepository<HRContact, Integer> {

    List<HRContact> findByCompany_CompanyId(Integer companyId);

    Optional<HRContact> findFirstByCompany_CompanyIdAndHrEmailIgnoreCase(Integer companyId, String hrEmail);
}
