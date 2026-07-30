package com.pmrs.backend.controller;

import com.pmrs.backend.dto.ReminderSettingsRequest;
import com.pmrs.backend.dto.ReminderSettingsResponse;
import com.pmrs.backend.service.DriveReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reminder Settings APIs", description = "On/off switch for the automatic drive-event reminder job")
@RestController
@RequestMapping("/reminder-settings")
@CrossOrigin("*")
public class ReminderSettingsController {

    private final DriveReminderService driveReminderService;

    @Value("${prms.reminder.days-before:7}")
    private int daysBefore;

    public ReminderSettingsController(DriveReminderService driveReminderService) {
        this.driveReminderService = driveReminderService;
    }

    @Operation(summary = "Get the automatic reminder job's current on/off state")
    @GetMapping
    public ReminderSettingsResponse getSettings() {
        return new ReminderSettingsResponse(driveReminderService.isAutoReminderEnabled(), daysBefore);
    }

    @Operation(summary = "Turn the automatic reminder job on or off")
    @PutMapping
    public ReminderSettingsResponse updateSettings(@RequestBody ReminderSettingsRequest request) {
        driveReminderService.setAutoReminderEnabled(request.isEnabled());
        return new ReminderSettingsResponse(driveReminderService.isAutoReminderEnabled(), daysBefore);
    }
}
