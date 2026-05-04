package cn.edu.zju.service;

import cn.edu.zju.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class DeepSeekClient {

    private static final String SYSTEM_PROMPT =
            "You are an assistant for a precision medicine matching system. "
                    + "Answer in Chinese by default. Provide general educational information only. "
                    + "Do not replace professional medical advice, diagnosis, or treatment decisions.";

    private final AppConfig config;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public DeepSeekClient() {
        this(AppConfig.getInstance(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    DeepSeekClient(AppConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    public boolean isConfigured() {
        return config.isDeepSeekConfigured();
    }

    public String chat(List<ChatMessage> conversation) throws IOException, InterruptedException {
        if (!isConfigured()) {
            throw new IllegalStateException("DeepSeek API key is not configured.");
        }
        if (conversation == null || conversation.isEmpty()) {
            throw new IllegalArgumentException("Message is required.");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("model", config.getDeepSeekModel());
        payload.addProperty("temperature", config.getDeepSeekTemperature());
        payload.addProperty("max_tokens", config.getDeepSeekMaxTokens());
        payload.addProperty("stream", false);
        payload.add("messages", buildMessages(conversation));

        HttpRequest request = HttpRequest.newBuilder(chatCompletionsUri())
                .timeout(Duration.ofSeconds(75))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getDeepSeekApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DeepSeek API request failed: HTTP " + response.statusCode()
                    + " - " + extractErrorMessage(response.body()));
        }
        return extractAssistantMessage(response.body());
    }

    private JsonArray buildMessages(List<ChatMessage> conversation) {
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        for (ChatMessage message : conversation) {
            if (message == null || !isSupportedRole(message.getRole()) || isBlank(message.getContent())) {
                continue;
            }
            JsonObject messageJson = new JsonObject();
            messageJson.addProperty("role", message.getRole());
            messageJson.addProperty("content", message.getContent());
            messages.add(messageJson);
        }
        return messages;
    }

    private URI chatCompletionsUri() {
        String baseUrl = config.getDeepSeekBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.deepseek.com";
        }
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/chat/completions")) {
            return URI.create(normalized);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return URI.create(normalized + "/chat/completions");
    }

    private String extractAssistantMessage(String responseBody) throws IOException {
        try {
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                throw new IOException("DeepSeek API returned no choices.");
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            JsonElement content = message == null ? null : message.get("content");
            if (content == null || content.isJsonNull()) {
                throw new IOException("DeepSeek API returned an empty message.");
            }
            return content.getAsString();
        } catch (IllegalStateException e) {
            throw new IOException("Failed to parse DeepSeek API response.", e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (isBlank(responseBody)) {
            return "empty response";
        }
        try {
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject error = responseJson.getAsJsonObject("error");
            JsonElement message = error == null ? null : error.get("message");
            if (message != null && !message.isJsonNull()) {
                return message.getAsString();
            }
        } catch (RuntimeException ignored) {
            // Fall through and return a short response preview.
        }
        return responseBody.length() > 300 ? responseBody.substring(0, 300) : responseBody;
    }

    private boolean isSupportedRole(String role) {
        return "user".equals(role) || "assistant".equals(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
