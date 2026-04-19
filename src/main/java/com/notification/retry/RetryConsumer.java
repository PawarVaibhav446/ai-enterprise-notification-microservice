package com.notification.retry;

import com.notification.dto.NotificationRequest;
import com.notification.dto.RetryMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryConsumer {

    private final RetryProducer retryProducer;

    @Value("${retry.max-attempts:3}")
    private int maxRetries;

    /**
     * Listen to retry queue and process messages
     */
    @RabbitListener(queues = "${rabbitmq.queues.retry}")
    public void consumeRetryMessage(RetryMessage retryMessage,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) {

        log.info("🔄 Received retry message - ID: {}, Attempt: {}/{}",
                retryMessage.getNotificationId(),
                retryMessage.getAttemptCount(),
                maxRetries);

        try {
            // Increment attempt count
            retryMessage.incrementAttempt();

            // Try to send notification again
            boolean success = retrySendNotification(retryMessage);

            if (success) {
                log.info("✅ Retry successful - ID: {}, Attempt: {}",
                        retryMessage.getNotificationId(),
                        retryMessage.getAttemptCount());

                // Acknowledge message (remove from queue)
                channel.basicAck(tag, false);

            } else {
                log.warn("❌ Retry failed - ID: {}, Attempt: {}/{}",
                        retryMessage.getNotificationId(),
                        retryMessage.getAttemptCount(),
                        maxRetries);

                // Check if max retries reached
                if (retryMessage.maxRetriesReached(maxRetries)) {
                    log.error("🔴 Max retries reached - ID: {}, Moving to DLQ",
                            retryMessage.getNotificationId());

                    // Reject message → goes to Dead Letter Queue
                    channel.basicReject(tag, false);

                } else {
                    // Send back to retry queue with incremented count
                    log.info("🔄 Sending back to retry queue - ID: {}, Next attempt: {}",
                            retryMessage.getNotificationId(),
                            retryMessage.getAttemptCount() + 1);

                    retryProducer.sendToRetryQueue(retryMessage);

                    // Acknowledge current message
                    channel.basicAck(tag, false);
                }
            }

        } catch (Exception e) {
            log.error("Error processing retry message - ID: {}",
                    retryMessage.getNotificationId(), e);

            try {
                // Reject and requeue on unexpected error
                channel.basicReject(tag, false);
            } catch (Exception ex) {
                log.error("Failed to reject message", ex);
            }
        }
    }

    /**
     * Actually retry sending the notification
     *
     * @param retryMessage - Message to retry
     * @return true if successful, false if failed
     */
    private boolean retrySendNotification(RetryMessage retryMessage) {

        log.debug("Attempting to send {} to {}",
                retryMessage.getType(), retryMessage.getTo());

        try {
            // Simulate sending notification
            // In real implementation, this would call actual providers

            String type = retryMessage.getType();
            String to = retryMessage.getTo();
            String subject = retryMessage.getSubject();
            String body = retryMessage.getBody();

            // Mock implementation - returns success
            switch (type.toUpperCase()) {
                case "EMAIL" -> {
                    log.info("📧 [RETRY] Sending EMAIL to: {}", to);
                    log.debug("   Subject: {}", subject);
                    log.debug("   Body: {}", body);
                    // emailService.send(to, subject, body);
                    return true; // Mock success
                }
                case "SMS" -> {
                    log.info("📱 [RETRY] Sending SMS to: {}", to);
                    log.debug("   Message: {}", body);
                    // smsService.send(to, body);
                    return true; // Mock success
                }
                case "WHATSAPP" -> {
                    log.info("💬 [RETRY] Sending WhatsApp to: {}", to);
                    log.debug("   Message: {}", body);
                    // whatsAppService.send(to, body);
                    return true; // Mock success
                }
                default -> {
                    log.error("Unknown notification type: {}", type);
                    return false;
                }
            }

        } catch (Exception e) {
            log.error("Error sending notification during retry", e);
            return false;
        }
    }
}