package com.pmrs.backend.controller;

import com.pmrs.backend.entity.AdminNotification;
import com.pmrs.backend.service.AdminNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notification APIs", description = "In-app alerts for placement-cell staff")
@RestController
@RequestMapping("/notifications")
@CrossOrigin("*")
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    public AdminNotificationController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "List the most recent notifications")
    @GetMapping
    public List<AdminNotification> list() {
        return notificationService.listRecent();
    }

    @Operation(summary = "Count unread notifications")
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.unreadCount());
    }

    @Operation(summary = "Mark a single notification as read")
    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Integer id) {
        notificationService.markRead(id);
    }

    @Operation(summary = "Mark all notifications as read")
    @PostMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllRead();
    }
}
