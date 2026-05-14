package vn.edu.tdtu.edocument.notification.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;
import vn.edu.tdtu.edocument.document.model.Document;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;
import vn.edu.tdtu.edocument.notification.core.UserProfile;

public class BrevoEmailNotification implements NotificationObserver {
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final int TEMPLATE_ID = 1;
    private static final String BREVO_API_KEY = "BREVO_API_KEY";
    private static final String BREVO_SENDER_EMAIL = "BREVO_SENDER_EMAIL";
    private static final String BREVO_SENDER_NAME = "BREVO_SENDER_NAME";

    @Override
    public void update(UserProfile userProfile, Document doc) {

        if (userProfile == null || !userProfile.isValidProfile()) {
            logFallback("Thông tin người nhận không hợp lệ.");
            return;
        }

        BrevoConfig config = BrevoConfig.fromEnv();
        if (!config.isValid()) {
            logFallback("Thiếu cấu hình BREVO_API_KEY/BREVO_SENDER_EMAIL/BREVO_SENDER_NAME.");
            return;
        }

        String requestBody = buildRequestBody(userProfile, doc, config);
        if (requestBody == null) {
            logFallback("Không có email người nhận hợp lệ.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL)).header("api-key", config.apiKey)
                .header("accept", "application/json").header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8)).build();

        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                System.out.println("[BREVO EMAIL] Gui email thanh cong. Status=" + status);
                System.out.println(response.body());
            } else {
                System.out.println("[BREVO EMAIL] Gui email that bai. Status=" + status + " Body=" + response.body());
                System.out.println(response.body());
            }
        } catch (IOException ex) {
            System.out.println("[BREVO EMAIL] Loi gui email: " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("[BREVO EMAIL] Loi gui email: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static void logFallback(String reason) {
        System.out.println("[BREVO EMAIL] Bo qua gui email, reason: " + reason);
    }

    private static String buildRequestBody(UserProfile userProfile, Document doc, BrevoConfig config) {
        JSONArray messageVersions = new JSONArray();
        appendVersion(messageVersions, doc, userProfile.getRole(), userProfile.getName(), userProfile.getEmail());

        if (messageVersions.isEmpty()) {
            return null;
        }

        String content = "Hồ sơ " + safe(doc.id) + " (" + safe(doc.documentType) + ") đã chuyển sang trạng thái "
                + safe(doc.status);

        JSONObject sender = new JSONObject().put("name", config.senderName).put("email", config.senderEmail);
        JSONObject params = new JSONObject().put("username", userProfile.getName()).put("content", content);
        JSONObject body = new JSONObject().put("sender", sender).put("templateId", TEMPLATE_ID)
                .put("messageVersions", messageVersions).put("params", params);

        return body.toString();
    }

    private static void appendVersion(JSONArray versions, Document doc, String role, String name, String email) {
        if (isBlank(email)) {
            return;
        }
        JSONObject recipient = new JSONObject().put("email", email).put("name", safe(name));
        JSONArray to = new JSONArray().put(recipient);

        JSONObject version = new JSONObject().put("to", to);

        versions.put(version);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static final class BrevoConfig {
        private final String apiKey;
        private final String senderEmail;
        private final String senderName;

        private BrevoConfig(String apiKey, String senderEmail, String senderName) {
            this.apiKey = apiKey;
            this.senderEmail = senderEmail;
            this.senderName = senderName;
        }

        private static BrevoConfig fromEnv() {
            Dotenv dotenv = Dotenv.load();
            return new BrevoConfig(dotenv.get(BREVO_API_KEY),
                    dotenv.get(BREVO_SENDER_EMAIL),
                    dotenv.get(BREVO_SENDER_NAME));
        }

        private boolean isValid() {
            return !isBlank(apiKey) && !isBlank(senderEmail) && !isBlank(senderName);
        }
    }
}
