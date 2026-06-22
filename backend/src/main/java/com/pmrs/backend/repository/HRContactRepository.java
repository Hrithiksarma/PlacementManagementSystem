package com.pmrs.backend.repository;

import com.pmrs.backend.entity.HRContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HRContactRepository extends JpaRepository<HRContact, Integer> {

    List<HRContact> findByCompany_CompanyId(Integer companyId);
}
