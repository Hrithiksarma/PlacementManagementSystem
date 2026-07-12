package com.academic.service.repository;

import com.academic.service.entity.AcademicUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicUserRepository extends JpaRepository<AcademicUser, Long> {

    Optional<AcademicUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
