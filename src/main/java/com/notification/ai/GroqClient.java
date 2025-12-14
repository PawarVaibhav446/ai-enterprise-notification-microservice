package com.notification.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GroqClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    public GroqClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Call Groq API to generate text
     * @param systemPrompt - Instructions for AI
     * @param userPrompt - User's actual request
     * @return Generated text
     */
    public String generateText(String systemPrompt, String userPrompt) {

        log.debug("Calling Groq API with model: {}", model);

        try {
            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 500
            );

            // Build HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Groq API error: Status {}", response.getStatusCode());
                throw new RuntimeException("Groq API call failed: " + response.getStatusCode());
            }

            String responseBody = response.getBody();
            log.debug("Groq API response received");

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String generatedText = jsonNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("AI text generated successfully - Length: {} chars", generatedText.length());
            return generatedText;

        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            throw new RuntimeException("Failed to generate AI text: " + e.getMessage(), e);
        }
    }
}