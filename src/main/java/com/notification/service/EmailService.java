package com.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    /**
     * Send email notification
     *
     * @param to - Recipient email address
     * @param subject - Email subject
     * @param body - Email body
     * @return true if sent successfully, false otherwise
     */
    public boolean send(String to, String subject, String body) {

        log.info("📧 EmailService: Sending email to {}", to);
        log.debug("   Subject: {}", subject);
        log.debug("   Body length: {} chars", body.length());

        try {
            // TODO: Integrate with real email provider
            // Example integrations:
            // 1. SendGrid API
            // 2. AWS SES
            // 3. SMTP server
            // 4. Mailgun

            // Mock implementation - simulate sending
            simulateEmailSending(to, subject, body);

            // Simulate 80% success rate for testing
            boolean success = Math.random() > 0.2;

            if (success) {
                log.info("✅ Email sent successfully to {}", to);
            } else {
                log.warn("❌ Email sending failed to {}", to);
            }

            return success;

        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
            return false;
        }
    }

    /**
     * Simulate email sending
     * In production, replace with actual API call
     */
    private void simulateEmailSending(String to, String subject, String body) {
        // Mock - just log the details
        log.debug("=== EMAIL PREVIEW ===");
        log.debug("To: {}", to);
        log.debug("Subject: {}", subject);
        log.debug("Body:\n{}", body);
        log.debug("====================");

        // Simulate network delay
        try {
            Thread.sleep(100); // 100ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Validate email address
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}