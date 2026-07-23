package com.pmrs.backend.service;

import com.pmrs.backend.dto.EligibleDriveDTO;
import com.pmrs.backend.dto.PlacementAchievementDTO;
import com.pmrs.backend.dto.PlacementOfferDTO;
import com.pmrs.backend.dto.PlacementStatusDTO;
import com.pmrs.backend.dto.StudentApplicationDTO;
import com.pmrs.backend.dto.StudentProfileDTO;

import java.util.List;

public interface StudentPortalService {

    StudentProfileDTO getProfile(String username);

    List<EligibleDriveDTO> getEligibleDrives(String username);

    List<StudentApplicationDTO> getApplications(String username);

    PlacementStatusDTO getPlacementStatus(String username);

    List<PlacementOfferDTO> getPlacementOffers(String username);

    void acceptOffer(String username, Integer applicationId);

    /** @return a message describing the rejection and the resulting penalty */
    String rejectOffer(String username, Integer applicationId);

    /** @return a message describing the withdrawal and any penalty applied */
    String withdrawApplication(String username, Integer applicationId);

    /** Decline an already-accepted offer — permanent bar + disciplinary referral. */
    String declineAcceptedOffer(String username, Integer applicationId);

    /** Penalty the student would incur by withdrawing/declining this application now. */
    java.util.Map<String, String> previewWithdrawalPenalty(String username, Integer applicationId);

    /** The student's current penalty standing (for banners and enforcement). */
    com.pmrs.backend.dto.PenaltyStatusDTO getPenaltyStatus(String username);

    void applyToDrive(String username, Integer driveId);

    List<PlacementAchievementDTO> getRecentAchievements();
}
