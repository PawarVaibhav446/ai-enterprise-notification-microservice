package com.notification.controller;

import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is UP! 🚀");
    }
}