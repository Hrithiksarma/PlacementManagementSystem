package com.pmrs.backend.service;

import com.pmrs.backend.dto.LoginRequest;
import com.pmrs.backend.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
