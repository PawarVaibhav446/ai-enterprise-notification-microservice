package com.notification.service;

import com.notification.ai.AiMessageService;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.dto.RetryMessage;
import com.notification.model.NotificationLog;
import com.notification.repository.NotificationRepository;
import com.notification.retry.RetryProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AiMessageService aiMessageService;
    private final RetryProducer retryProducer;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;

    /**
     * Main method to send notifications
     * 1. Generate message using AI
     * 2. Send via appropriate channel
     * 3. Save to MongoDB
     * 4. If fails → Send to retry queue
     * 5. Return response
     */
    public NotificationResponse sendNotification(NotificationRequest request) {

        String notificationId = UUID.randomUUID().toString();

        log.info("Processing notification - ID: {}, Type: {}, Intent: {}",
                notificationId, request.getType(), request.getIntent());

        // Create log entry
        NotificationLog notificationLog = NotificationLog.builder()
                .notificationId(notificationId)
                .type(request.getType().toString())
                .intent(request.getIntent())
                .tone(request.getTone())
                .recipient(request.getTo())
                .variables(request.getVariables())
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

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

            // Update log with generated message
            notificationLog.setSubject(subject);
            notificationLog.setBody(body);
            notificationLog.setAiGenerated(true);

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
                // Step 3: Update status and save to MongoDB
                notificationLog.setStatus("SUCCESS");
                notificationLog.setUpdatedAt(LocalDateTime.now());
                notificationRepository.save(notificationLog);

                log.info("Notification sent and saved successfully - ID: {}", notificationId);

                return NotificationResponse.success(
                        notificationId,
                        "Notification sent successfully via " + request.getType()
                );
            } else {
                // Failed → Send to retry queue
                log.warn("Notification failed, queuing for retry - ID: {}", notificationId);

                notificationLog.setStatus("QUEUED_FOR_RETRY");
                notificationLog.setUpdatedAt(LocalDateTime.now());
                notificationRepository.save(notificationLog);

                RetryMessage retryMessage = RetryMessage.builder()
                        .notificationId(notificationId)
                        .type(request.getType().toString())
                        .to(request.getTo())
                        .subject(subject)
                        .body(body)
                        .attemptCount(0)
                        .originalIntent(request.getIntent())
                        .build();

                retryProducer.sendToRetryQueue(retryMessage);

                return NotificationResponse.queued(notificationId);
            }

        } catch (Exception e) {
            log.error("Error sending notification - ID: {}", notificationId, e);

            // Save error to MongoDB
            notificationLog.setStatus("FAILED");
            notificationLog.setErrorMessage(e.getMessage());
            notificationLog.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(notificationLog);

            return NotificationResponse.failed("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Route to appropriate channel using dedicated services
     */
    private boolean sendViaChannel(NotificationRequest.NotificationType type,
                                   String to,
                                   String subject,
                                   String body) {

        return switch (type) {
            case EMAIL -> emailService.send(to, subject, body);
            case SMS -> smsService.send(to, body);
            case WHATSAPP -> whatsAppService.send(to, body);
        };
    }
}