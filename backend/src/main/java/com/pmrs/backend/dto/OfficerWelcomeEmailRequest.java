package com.pmrs.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class OfficerWelcomeEmailRequest {

    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;

    private String comment;

    public String getTemporaryPassword() { return temporaryPassword; }
    public void   setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }
    public String getComment() { return comment; }
    public void   setComment(String comment) { this.comment = comment; }
}
