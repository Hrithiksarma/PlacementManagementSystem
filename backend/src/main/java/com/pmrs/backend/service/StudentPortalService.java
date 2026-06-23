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

    void rejectOffer(String username, Integer applicationId);

    void applyToDrive(String username, Integer driveId);

    List<PlacementAchievementDTO> getRecentAchievements();
}
