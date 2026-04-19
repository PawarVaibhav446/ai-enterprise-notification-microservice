package com.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryMessage implements Serializable {

    private String notificationId;
    private String type;  // EMAIL, SMS, WHATSAPP
    private String to;
    private String subject;  // Only for EMAIL
    private String body;
    private int attemptCount;  // How many times we've tried
    private String originalIntent;  // For logging

    /**
     * Increment retry attempt
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }

    /**
     * Check if max retries reached
     */
    public boolean maxRetriesReached(int maxRetries) {
        return this.attemptCount >= maxRetries;
    }
}