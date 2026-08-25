package com.pmrs.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateJdRequest {
    @NotBlank(message = "JD URL is required")
    private String jdUrl;

    public String getJdUrl() { return jdUrl; }
    public void setJdUrl(String jdUrl) { this.jdUrl = jdUrl; }
}
