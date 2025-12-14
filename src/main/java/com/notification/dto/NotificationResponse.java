package com.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String status;
    private String message;
    private Long timestamp;

    // Success response
    public static NotificationResponse success(String id, String message) {
        return NotificationResponse.builder()
                .id(id)
                .status("SUCCESS")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Failed response
    public static NotificationResponse failed(String message) {
        return NotificationResponse.builder()
                .status("FAILED")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Queued for retry response
    public static NotificationResponse queued(String id) {
        return NotificationResponse.builder()
                .id(id)
                .status("QUEUED_FOR_RETRY")
                .message("Notification queued for retry")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}