package com.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    /**
     * Send SMS notification
     *
     * @param to - Recipient phone number (E.164 format)
     * @param message - SMS message content
     * @return true if sent successfully, false otherwise
     */
    public boolean send(String to, String message) {

        log.info("📱 SmsService: Sending SMS to {}", to);
        log.debug("   Message length: {} chars", message.length());

        try {
            // TODO: Integrate with real SMS provider
            // Example integrations:
            // 1. Twilio API
            // 2. AWS SNS
            // 3. Nexmo/Vonage
            // 4. MessageBird

            // Validate phone number
            if (!isValidPhoneNumber(to)) {
                log.error("Invalid phone number format: {}", to);
                return false;
            }

            // Check message length (160 chars for single SMS)
            if (message.length() > 160) {
                log.warn("SMS message exceeds 160 chars ({} chars) - will be sent as multiple messages",
                        message.length());
            }

            // Mock implementation - simulate sending
            simulateSmsSending(to, message);

            // Simulate 80% success rate for testing
            boolean success = Math.random() > 0.2;

            if (success) {
                log.info("✅ SMS sent successfully to {}", to);
            } else {
                log.warn("❌ SMS sending failed to {}", to);
            }

            return success;

        } catch (Exception e) {
            log.error("Error sending SMS to {}", to, e);
            return false;
        }
    }

    /**
     * Simulate SMS sending
     * In production, replace with actual API call
     */
    private void simulateSmsSending(String to, String message) {
        // Mock - just log the details
        log.debug("=== SMS PREVIEW ===");
        log.debug("To: {}", to);
        log.debug("Message:\n{}", message);
        log.debug("==================");

        // Simulate network delay
        try {
            Thread.sleep(50); // 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Validate phone number (basic E.164 format)
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }
}