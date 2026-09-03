package com.pmrs.backend.repository;

import com.pmrs.backend.entity.ResumeJdMatchSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeJdMatchSkillRepository extends JpaRepository<ResumeJdMatchSkill, Integer> {

    List<ResumeJdMatchSkill> findByMatch_MatchId(Integer matchId);

    List<ResumeJdMatchSkill> findByMatch_Drive_DriveId(Integer driveId);
}
