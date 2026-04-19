package com.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;

    private String notificationId;  // Our UUID

    private String type;  // EMAIL, SMS, WHATSAPP

    private String intent;  // order_shipped, otp, etc.

    private String tone;  // friendly, formal, etc.

    private String recipient;  // Email or phone number

    private String subject;  // Only for EMAIL

    private String body;  // Message content

    private Map<String, Object> variables;  // Original variables

    private String status;  // SUCCESS, FAILED, QUEUED_FOR_RETRY

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer retryCount;  // How many times retried

    private String errorMessage;  // If failed

    private Boolean aiGenerated;  // Was message AI-generated or fallback?
}