package com.notification.ai;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    /**
     * Build system prompt - Instructions for AI
     */
    public String buildSystemPrompt(String tone, String type) {

        String basePrompt = """
            You are an enterprise notification generator.
            Generate concise, clear, personalized messages.
            Do not include any preamble or explanation.
            Generate ONLY the notification content.
            """;

        String toneInstruction = switch (tone.toLowerCase()) {
            case "formal" -> "Use professional, business-appropriate language.";
            case "friendly" -> "Use warm, conversational language with emojis when appropriate.";
            case "marketing" -> "Use persuasive, exciting language that drives action.";
            case "strict" -> "Use direct, authoritative language without pleasantries.";
            default -> "Use neutral, clear language.";
        };

        String channelInstruction = switch (type.toUpperCase()) {
            case "EMAIL" -> """
                Format:
                Subject: [Clear subject line]
                
                [Email body with greeting and content]
                """;
            case "SMS" -> "Keep it under 160 characters. Be extremely concise.";
            case "WHATSAPP" -> "Keep it conversational and under 300 characters.";
            default -> "Keep it concise and clear.";
        };

        return basePrompt + "\n" + toneInstruction + "\n" + channelInstruction;
    }

    /**
     * Build user prompt - Actual request with variables
     */
    public String buildUserPrompt(String intent, Map<String, Object> variables) {

        String intentDescription = getIntentDescription(intent);
        String variablesString = formatVariables(variables);

        return String.format("""
            Intent: %s
            
            %s
            
            Variables to use:
            %s
            
            Generate the notification message now.
            """, intent, intentDescription, variablesString);
    }

    /**
     * Get description for each intent
     */
    private String getIntentDescription(String intent) {
        return switch (intent.toLowerCase()) {
            case "order_shipped" ->
                    "Generate a notification that an order has been shipped. Include order ID and expected delivery date.";

            case "order_delivered" ->
                    "Generate a notification that an order has been delivered successfully.";

            case "otp" ->
                    "Generate a one-time password (OTP) notification. Keep it very short and include the OTP prominently.";

            case "account_alert" ->
                    "Generate a security alert about account activity. Be clear and actionable.";

            case "payment_received" ->
                    "Generate a confirmation that payment has been received. Include amount and transaction ID.";

            case "payment_failed" ->
                    "Generate a notification that payment failed. Be helpful and suggest next steps.";

            case "welcome" ->
                    "Generate a welcome message for a new user. Be warm and encouraging.";

            case "password_reset" ->
                    "Generate a password reset notification with a link or instructions.";

            case "offer" ->
                    "Generate a promotional offer notification. Be exciting and include the offer details.";

            case "reminder" ->
                    "Generate a reminder notification about an upcoming event or deadline.";

            case "subscription_expiring" ->
                    "Generate a notification that subscription is expiring soon. Include renewal instructions.";

            case "feedback_request" ->
                    "Generate a polite request for feedback or review.";

            default ->
                    "Generate a notification based on the provided variables.";
        };
    }

    /**
     * Format variables into readable string
     */
    private String formatVariables(Map<String, Object> variables) {
        return variables.entrySet().stream()
                .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}