package org.example.tahadaw.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Single entry point for all AI API calls in the project.
 * <p>
 * {@link #ask(String)} always requests a JSON object from the API ({@code response_format: json_object}).
 * Put the exact JSON shape you need inside {@code prompt}.
 * <p>
 * GPT-5.x / o-series models use {@code max_completion_tokens} and {@code reasoning_effort} instead of
 * the legacy {@code max_tokens} + {@code temperature} pair used by gpt-4o-mini.
 */
@Service
public class AiService {

    private static final String JSON_SYSTEM_MESSAGE =
            "You must respond with a single valid JSON object only. No markdown, no code fences, no extra text.";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${ai.model:gpt-5.5}")
    private String model;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    /** Mapped to max_completion_tokens on GPT-5 / o-series; max_tokens on legacy chat models. */
    @Value("${ai.max-tokens:4000}")
    private int maxTokens;

    /** GPT-5 only: none | low | medium | high | xhigh. Ignored for legacy models. */
    @Value("${ai.reasoning-effort:low}")
    private String reasoningEffort;

    /**
     * Sends a prompt to the chat API and returns a JSON string.
     *
     * @param prompt instructions plus the JSON schema you want
     * @return JSON object as text, starting with {@code {}
     */
    public String ask(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiException(
                    "AI is not configured. Add openai.api.key to application-local.properties (copy from application-local.properties.example).");
        }

        Map<String, Object> body = buildRequestBody(prompt);

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            String responseBody = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String content = normalizeJsonContent(extractAssistantContent(responseBody));
            validateJsonShape(content);
            return content;
        } catch (AiException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            throw new AiException("AI API error: " + ex.getStatusCode() + " — " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new AiException("AI request failed: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", JSON_SYSTEM_MESSAGE),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));

        if (usesReasoningModelParams()) {
            body.put("max_completion_tokens", maxTokens);
            body.put("reasoning_effort", reasoningEffort);
        } else {
            body.put("max_tokens", maxTokens);
            body.put("temperature", 0);
        }
        return body;
    }

    /**
     * GPT-5 / o-series chat models reject {@code max_tokens} and expect {@code max_completion_tokens}.
     */
    static boolean usesReasoningModelParams(String model) {
        if (model == null || model.isBlank()) {
            return false;
        }
        String normalized = model.toLowerCase(Locale.ROOT);
        return normalized.startsWith("gpt-5")
                || normalized.startsWith("o1")
                || normalized.startsWith("o3")
                || normalized.startsWith("o4");
    }

    private boolean usesReasoningModelParams() {
        return usesReasoningModelParams(model);
    }

    static String normalizeJsonContent(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int closingFence = trimmed.lastIndexOf("```");
            if (closingFence >= 0) {
                trimmed = trimmed.substring(0, closingFence);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }

    static void validateJsonShape(String content) {
        if (content.isEmpty()) {
            throw new AiException("AI returned an empty response.");
        }
        char first = content.charAt(0);
        if (first != '{' && first != '[') {
            throw new AiException("AI response is not JSON. Starts with: " + content.substring(0, Math.min(40, content.length())));
        }
    }

    static String extractAssistantContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiException("AI returned an empty response.");
        }
        try {
            JsonNode root = JSON.readTree(responseBody);
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String message = error.path("message").asText("Unknown AI error");
                throw new AiException("AI API error: " + message);
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiException("AI response did not contain choices.");
            }

            JsonNode message = choices.get(0).path("message");
            JsonNode refusal = message.path("refusal");
            if (refusal.isTextual() && !refusal.asText().isBlank()) {
                throw new AiException("AI refused the request: " + refusal.asText());
            }

            JsonNode content = message.path("content");
            if (content.isNull() || !content.isTextual() || content.asText().isBlank()) {
                throw new AiException("AI returned an empty response.");
            }
            return content.asText();
        } catch (AiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiException("Failed to parse AI response: " + ex.getMessage(), ex);
        }
    }
}
