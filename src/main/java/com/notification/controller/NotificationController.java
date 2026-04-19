package com.notification.controller;

import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.model.NotificationLog;
import com.notification.repository.NotificationRepository;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * Send notification (Email/SMS/WhatsApp)
     * AI will generate the message content
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("Received notification request - Type: {}, Intent: {}, To: {}",
                request.getType(), request.getIntent(), request.getTo());

        try {
            NotificationResponse response = notificationService.sendNotification(request);

            if ("SUCCESS".equals(response.getStatus())) {
                log.info("Notification sent successfully - ID: {}", response.getId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Notification failed or queued - Status: {}", response.getStatus());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }

        } catch (Exception e) {
            log.error("Error processing notification request", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(NotificationResponse.failed("Error: " + e.getMessage()));
        }
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationLog> getNotification(@PathVariable String notificationId) {
        log.info("Fetching notification - ID: {}", notificationId);

        return notificationRepository.findByNotificationId(notificationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all notifications (recent 10)
     */
    @GetMapping("/history")
    public ResponseEntity<List<NotificationLog>> getRecentNotifications() {
        log.info("Fetching recent notifications");

        List<NotificationLog> logs = notificationRepository.findTop10ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get notifications by recipient
     */
    @GetMapping("/history/recipient/{recipient}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByRecipient(
            @PathVariable String recipient) {
        log.info("Fetching notifications for recipient: {}", recipient);

        List<NotificationLog> logs = notificationRepository.findByRecipient(recipient);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get notifications by status
     */
    @GetMapping("/history/status/{status}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByStatus(
            @PathVariable String status) {
        log.info("Fetching notifications with status: {}", status);

        List<NotificationLog> logs = notificationRepository.findByStatus(status);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/history/type/{type}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByType(
            @PathVariable String type) {
        log.info("Fetching notifications of type: {}", type);

        List<NotificationLog> logs = notificationRepository.findByType(type);
        return ResponseEntity.ok(logs);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is UP! 🚀");
    }
}