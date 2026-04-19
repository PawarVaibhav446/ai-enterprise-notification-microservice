package com.notification.retry;

import com.notification.dto.RetryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.retry}")
    private String retryExchange;

    @Value("${rabbitmq.routing-keys.retry}")
    private String retryRoutingKey;

    @Value("${retry.initial-delay:2000}")
    private long initialDelay;

    @Value("${retry.multiplier:2}")
    private int multiplier;

    /**
     * Send notification to retry queue
     *
     * @param retryMessage - Message to retry
     */
    public void sendToRetryQueue(RetryMessage retryMessage) {

        log.info("Sending notification to retry queue - ID: {}, Attempt: {}",
                retryMessage.getNotificationId(), retryMessage.getAttemptCount());

        try {
            // Calculate delay based on attempt count (exponential backoff)
            long delay = calculateDelay(retryMessage.getAttemptCount());

            log.debug("Retry delay: {} ms", delay);

            // Create message with delay
            MessagePostProcessor messagePostProcessor = message -> {
                message.getMessageProperties().setDelay((int) delay);
                return message;
            };

            // Send to RabbitMQ
            rabbitTemplate.convertAndSend(
                    retryExchange,
                    retryRoutingKey,
                    retryMessage,
                    messagePostProcessor
            );

            log.info("Notification queued for retry - ID: {}, Will retry in {} ms",
                    retryMessage.getNotificationId(), delay);

        } catch (Exception e) {
            log.error("Failed to send message to retry queue - ID: {}",
                    retryMessage.getNotificationId(), e);
        }
    }

    /**
     * Calculate retry delay with exponential backoff
     *
     * Attempt 1: 2 seconds
     * Attempt 2: 4 seconds
     * Attempt 3: 8 seconds
     */
    private long calculateDelay(int attemptCount) {
        return initialDelay * (long) Math.pow(multiplier, attemptCount - 1);
    }
}