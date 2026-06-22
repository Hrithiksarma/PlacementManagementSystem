package com.pmrs.backend.repository;

import com.pmrs.backend.entity.Drive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriveRepository extends JpaRepository<Drive, Integer> {

    List<Drive> findByStatus(String status);

    List<Drive> findByCompany_CompanyId(Integer companyId);

    List<Drive> findByRoleOffered(String roleOffered);

    List<Drive> findByDriveType(String driveType);
}
