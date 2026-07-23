package com.pmrs.backend.service;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;

import java.util.List;
import java.util.Map;

public interface PenaltyService {

    /**
     * Issues the penalty owed for withdrawing the given application.
     * No-op (returns null) when the stage carries no penalty.
     *
     * @param stageAtWithdrawal the application status BEFORE it was set to Withdrawn
     * @return a human-readable description of the penalty applied, or null
     */
    String applyWithdrawalPenalty(Application application, String stageAtWithdrawal);

    /** Permanent bar + disciplinary referral for declining an accepted offer. */
    String applyDeclinePenalty(Application application);

    /**
     * What penalty WOULD be applied if the student withdrew/declined this
     * application right now — for the confirmation dialog. Keys:
     * "penaltyType" (NONE / SKIP_DRIVES / TIME_BAN / PERMANENT_BAN / BLOCKED)
     * and "message".
     */
    Map<String, String> previewPenalty(String applicationStatus);

    /** The student's resolved current bar (expiry is applied lazily here). */
    PenaltyStatusDTO getPenaltyStatus(Integer studentId);

    /**
     * Called whenever a new drive opens: decrements the SKIP_DRIVES counter
     * of every penalized student who would have been eligible for it.
     */
    void onDriveCreated(Drive drive);

    List<PenaltyDTO> getAllPenalties();

    PenaltyDTO liftPenalty(Integer penaltyId, String adminUsername);
}
