package com.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Type is required (EMAIL, SMS, or WHATSAPP)")
    private NotificationType type;

    @NotBlank(message = "Intent is required (e.g., order_shipped, otp, account_alert)")
    private String intent;

    @NotBlank(message = "Recipient is required")
    private String to;

    private String tone = "friendly";

    @NotNull(message = "Variables are required")
    private Map<String, Object> variables;

    public enum NotificationType {
        EMAIL,
        SMS,
        WHATSAPP
    }
}