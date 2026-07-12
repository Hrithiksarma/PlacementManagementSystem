package com.pmrs.backend.service;

import com.pmrs.backend.dto.CreateOfficerRequest;
import com.pmrs.backend.dto.OfficerAccountDTO;
import com.pmrs.backend.dto.OfficerCreatedDTO;

import java.util.List;

public interface OfficerAccountService {

    OfficerCreatedDTO createOfficer(CreateOfficerRequest request);

    List<OfficerAccountDTO> listOfficers();

    void setEnabled(Long userId, boolean enabled);
}
