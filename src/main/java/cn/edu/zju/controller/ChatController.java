package cn.edu.zju.controller;

import cn.edu.zju.AppConfig;
import cn.edu.zju.service.DeepSeekClient;
import cn.edu.zju.servlet.DispatchServlet;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_HISTORY_MESSAGES = 8;

    private final Gson gson = new Gson();
    private final DeepSeekClient deepSeekClient = new DeepSeekClient();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/assistant", this::assistant);
        dispatcher.registerPostMapping("/assistant/ask", this::ask);
    }

    public void assistant(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("deepSeekConfigured", AppConfig.getInstance().isDeepSeekConfigured());
        request.getRequestDispatcher("/views/assistant.jsp").forward(request, response);
    }

    public void ask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            AskRequest askRequest = gson.fromJson(readBody(request), AskRequest.class);
            String message = trimToEmpty(askRequest == null ? null : askRequest.message);
            if (message.isEmpty()) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ChatResponse.error("Message is required."));
                return;
            }
            if (message.length() > MAX_MESSAGE_LENGTH) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ChatResponse.error("Message is too long."));
                return;
            }

            List<DeepSeekClient.ChatMessage> conversation = buildConversation(askRequest.history, message);
            String answer = deepSeekClient.chat(conversation);
            writeJson(response, HttpServletResponse.SC_OK, ChatResponse.success(answer));
        } catch (JsonSyntaxException | IllegalArgumentException e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, ChatResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            writeJson(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, ChatResponse.error(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeJson(response, HttpServletResponse.SC_BAD_GATEWAY,
                    ChatResponse.error("DeepSeek request was interrupted."));
        } catch (IOException e) {
            log.warn("DeepSeek request failed.", e);
            writeJson(response, HttpServletResponse.SC_BAD_GATEWAY,
                    ChatResponse.error("DeepSeek request failed. Please try again later."));
        }
    }

    private List<DeepSeekClient.ChatMessage> buildConversation(List<DeepSeekClient.ChatMessage> history, String message) {
        List<DeepSeekClient.ChatMessage> conversation = new ArrayList<>();
        if (history != null) {
            int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
            for (int i = start; i < history.size(); i++) {
                DeepSeekClient.ChatMessage historyMessage = history.get(i);
                if (historyMessage == null || !isAllowedRole(historyMessage.getRole())) {
                    continue;
                }
                String content = trimToEmpty(historyMessage.getContent());
                if (!content.isEmpty()) {
                    conversation.add(new DeepSeekClient.ChatMessage(
                            historyMessage.getRole(), trimToMax(content, MAX_MESSAGE_LENGTH)));
                }
            }
        }
        conversation.add(new DeepSeekClient.ChatMessage("user", message));
        return conversation;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }

    private void writeJson(HttpServletResponse response, int status, ChatResponse payload) throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(payload));
    }

    private boolean isAllowedRole(String role) {
        return "user".equals(role) || "assistant".equals(role);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToMax(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static class AskRequest {
        private String message;
        private List<DeepSeekClient.ChatMessage> history;
    }

    private static class ChatResponse {
        private final boolean success;
        private final String answer;
        private final String error;

        private ChatResponse(boolean success, String answer, String error) {
            this.success = success;
            this.answer = answer;
            this.error = error;
        }

        private static ChatResponse success(String answer) {
            return new ChatResponse(true, answer, null);
        }

        private static ChatResponse error(String error) {
            return new ChatResponse(false, null, error);
        }
    }
}
