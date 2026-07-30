package com.pmrs.backend.dto;

public class ReminderSettingsResponse {
    private final boolean enabled;
    private final int     daysBefore;

    public ReminderSettingsResponse(boolean enabled, int daysBefore) {
        this.enabled = enabled;
        this.daysBefore = daysBefore;
    }

    public boolean isEnabled()    { return enabled; }
    public int     getDaysBefore() { return daysBefore; }
}
