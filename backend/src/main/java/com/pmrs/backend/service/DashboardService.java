package com.pmrs.backend.service;

import com.pmrs.backend.dto.DashboardDTO;

public interface DashboardService {
    DashboardDTO getDashboardData(Integer year, String branch, String program);
}
