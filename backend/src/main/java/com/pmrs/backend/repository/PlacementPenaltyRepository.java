package com.pmrs.backend.repository;

import com.pmrs.backend.entity.PlacementPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementPenaltyRepository extends JpaRepository<PlacementPenalty, Integer> {

    List<PlacementPenalty> findByStudent_StudentIdAndActiveTrue(Integer studentId);

    List<PlacementPenalty> findByActiveTrue();

    List<PlacementPenalty> findAllByOrderByPenaltyIdDesc();
}
