package com.notification.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMessageService {

    private final GroqClient groqClient;
    private final PromptBuilder promptBuilder;

    /**
     * Generate message using AI
     * Result is cached to avoid repeated API calls
     */
    @Cacheable(value = "aiMessages", key = "#intent + '-' + #tone + '-' + #type + '-' + #variables.hashCode()")
    public GeneratedMessage generateMessage(String intent, String tone, String type, Map<String, Object> variables) {

        log.info("Generating AI message - Intent: {}, Tone: {}, Type: {}", intent, tone, type);

        try {
            // Build prompts
            String systemPrompt = promptBuilder.buildSystemPrompt(tone, type);
            String userPrompt = promptBuilder.buildUserPrompt(intent, variables);

            log.debug("System prompt: {}", systemPrompt);
            log.debug("User prompt: {}", userPrompt);

            // Call AI
            String generatedContent = groqClient.generateText(systemPrompt, userPrompt);

            // Parse content based on type
            GeneratedMessage message = parseGeneratedContent(generatedContent, type);

            log.info("AI message generated successfully - Subject: {}", message.getSubject());
            return message;

        } catch (Exception e) {
            log.error("AI generation failed for intent: {}", intent, e);

            // Return fallback message if AI fails
            log.warn("Using fallback message for intent: {}", intent);
            return getFallbackMessage(intent, variables, type);
        }
    }

    /**
     * Parse AI-generated content based on channel type
     */
    private GeneratedMessage parseGeneratedContent(String content, String type) {

        if ("EMAIL".equalsIgnoreCase(type)) {
            // Email format: "Subject: ...\n\n[body]"
            String[] parts = content.split("\n\n", 2);

            if (parts.length >= 2) {
                String subject = parts[0].replace("Subject:", "").trim();
                String body = parts[1].trim();
                return new GeneratedMessage(subject, body);
            } else {
                // If AI didn't follow format, use first line as subject
                String[] lines = content.split("\n", 2);
                String subject = lines[0].replace("Subject:", "").trim();
                String body = lines.length > 1 ? lines[1].trim() : content;
                return new GeneratedMessage(subject, body);
            }
        } else {
            // SMS/WhatsApp: No subject, just body
            return new GeneratedMessage(null, content.trim());
        }
    }

    /**
     * Fallback messages when AI fails
     */
    private GeneratedMessage getFallbackMessage(String intent, Map<String, Object> variables, String type) {

        return switch (intent.toLowerCase()) {
            case "order_shipped" -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Order Shipped" : null,
                    String.format("Your order %s has been shipped and will arrive soon!",
                            variables.getOrDefault("orderId", ""))
            );

            case "otp" -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Verification Code" : null,
                    String.format("Your OTP is: %s. Valid for %s minutes.",
                            variables.getOrDefault("otp", ""),
                            variables.getOrDefault("expiryMinutes", "5"))
            );

            case "welcome" -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Welcome!" : null,
                    String.format("Welcome %s! We're excited to have you with us.",
                            variables.getOrDefault("name", ""))
            );

            case "payment_received" -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Payment Received" : null,
                    String.format("Payment of %s received successfully. Transaction ID: %s",
                            variables.getOrDefault("amount", ""),
                            variables.getOrDefault("transactionId", ""))
            );

            case "account_alert" -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Account Alert" : null,
                    String.format("Security alert: %s. If this wasn't you, please contact support immediately.",
                            variables.getOrDefault("action", "unusual activity detected"))
            );

            default -> new GeneratedMessage(
                    "EMAIL".equalsIgnoreCase(type) ? "Notification" : null,
                    "You have a new notification. Please check your account for details."
            );
        };
    }
}

