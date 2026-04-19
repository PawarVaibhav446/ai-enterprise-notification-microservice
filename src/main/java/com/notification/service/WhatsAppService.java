package com.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsAppService {

    /**
     * Send WhatsApp notification
     *
     * @param to - Recipient WhatsApp number (E.164 format)
     * @param message - WhatsApp message content
     * @return true if sent successfully, false otherwise
     */
    public boolean send(String to, String message) {

        log.info("💬 WhatsAppService: Sending WhatsApp message to {}", to);
        log.debug("   Message length: {} chars", message.length());

        try {
            // TODO: Integrate with WhatsApp Business API
            // Example integrations:
            // 1. WhatsApp Business API (Meta)
            // 2. Twilio WhatsApp API
            // 3. MessageBird WhatsApp API
            // 4. 360Dialog

            // Validate phone number
            if (!isValidPhoneNumber(to)) {
                log.error("Invalid WhatsApp number format: {}", to);
                return false;
            }

            // WhatsApp supports longer messages (up to 4096 chars)
            if (message.length() > 4096) {
                log.error("WhatsApp message exceeds 4096 chars limit ({} chars)", message.length());
                return false;
            }

            // Mock implementation - simulate sending
            simulateWhatsAppSending(to, message);

            // Simulate 80% success rate for testing
            boolean success = Math.random() > 0.2;

            if (success) {
                log.info("✅ WhatsApp message sent successfully to {}", to);
            } else {
                log.warn("❌ WhatsApp sending failed to {}", to);
            }

            return success;

        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}", to, e);
            return false;
        }
    }

    /**
     * Simulate WhatsApp sending
     * In production, replace with actual API call
     */
    private void simulateWhatsAppSending(String to, String message) {
        // Mock - just log the details
        log.debug("=== WHATSAPP PREVIEW ===");
        log.debug("To: {}", to);
        log.debug("Message:\n{}", message);
        log.debug("=======================");

        // Simulate network delay
        try {
            Thread.sleep(75); // 75ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Validate phone number (E.164 format)
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }
}