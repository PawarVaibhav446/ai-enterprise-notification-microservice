package com.notification.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generated message structure
 */
@Data
@AllArgsConstructor
public class GeneratedMessage {
    private String subject;  // Only for EMAIL, null for SMS/WhatsApp
    private String body;
}
