package com.notification.service;

import com.notification.ai.AiMessageService;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.dto.RetryMessage;
import com.notification.retry.RetryProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AiMessageService aiMessageService;
    private final RetryProducer retryProducer;

    /**
     * Main method to send notifications
     * 1. Generate message using AI
     * 2. Send via appropriate channel
     * 3. If fails → Send to retry queue
     * 4. Return response
     */
    public NotificationResponse sendNotification(NotificationRequest request) {

        String notificationId = UUID.randomUUID().toString();

        log.info("Processing notification - ID: {}, Type: {}, Intent: {}",
                notificationId, request.getType(), request.getIntent());

        try {
            // Step 1: Generate AI message
            log.debug("Generating AI message for intent: {}", request.getIntent());

            var aiMessage = aiMessageService.generateMessage(
                    request.getIntent(),
                    request.getTone(),
                    request.getType().toString(),
                    request.getVariables()
            );

            String subject = aiMessage.getSubject();
            String body = aiMessage.getBody();

            log.info("AI Message generated - Subject: {}, Body length: {} chars",
                    subject, body.length());

            // Step 2: Send via appropriate channel
            boolean sent = sendViaChannel(
                    request.getType(),
                    request.getTo(),
                    subject,
                    body
            );

            if (sent) {
                log.info("Notification sent successfully - ID: {}", notificationId);
                return NotificationResponse.success(
                        notificationId,
                        "Notification sent successfully via " + request.getType()
                );
            } else {
                // Step 3: Failed → Send to retry queue
                log.warn("Notification failed, queuing for retry - ID: {}", notificationId);

                RetryMessage retryMessage = RetryMessage.builder()
                        .notificationId(notificationId)
                        .type(request.getType().toString())
                        .to(request.getTo())
                        .subject(subject)
                        .body(body)
                        .attemptCount(0)  // First attempt will be 1
                        .originalIntent(request.getIntent())
                        .build();

                retryProducer.sendToRetryQueue(retryMessage);

                return NotificationResponse.queued(notificationId);
            }

        } catch (Exception e) {
            log.error("Error sending notification - ID: {}", notificationId, e);
            return NotificationResponse.failed("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Route to appropriate channel (Email/SMS/WhatsApp)
     */
    private boolean sendViaChannel(NotificationRequest.NotificationType type,
                                   String to,
                                   String subject,
                                   String body) {

        return switch (type) {
            case EMAIL -> {
                log.info("📧 Sending EMAIL to: {}", to);
                log.info("   Subject: {}", subject);
                log.info("   Body: {}", body);
                // emailService.send(to, subject, body);

                // Simulate random failure for testing retry (20% failure rate)
                boolean success = Math.random() > 0.2;
                if (!success) {
                    log.warn("❌ Email send failed (simulated)");
                }
                yield success;
            }
            case SMS -> {
                log.info("📱 Sending SMS to: {}", to);
                log.info("   Message: {}", body);
                // smsService.send(to, body);

                boolean success = Math.random() > 0.2;
                if (!success) {
                    log.warn("❌ SMS send failed (simulated)");
                }
                yield success;
            }
            case WHATSAPP -> {
                log.info("💬 Sending WhatsApp to: {}", to);
                log.info("   Message: {}", body);
                // whatsAppService.send(to, body);

                boolean success = Math.random() > 0.2;
                if (!success) {
                    log.warn("❌ WhatsApp send failed (simulated)");
                }
                yield success;
            }
        };
    }
}