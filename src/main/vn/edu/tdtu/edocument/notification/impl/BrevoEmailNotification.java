package vn.edu.tdtu.edocument.notification.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.UserPreference;
import vn.edu.tdtu.edocument.notification.core.NotificationObserver;

public class BrevoEmailNotification implements NotificationObserver {
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final int TEMPLATE_ID = 1;
    private static final String ENV_API_KEY = "BREVO_API_KEY";
    private static final String ENV_SENDER_EMAIL = "BREVO_SENDER_EMAIL";
    private static final String ENV_SENDER_NAME = "BREVO_SENDER_NAME";

    @Override
    public void update(Document doc) {
        if (!isEnabled(doc)) {
            return;
        }
        BrevoConfig config = BrevoConfig.fromEnv();
        if (!config.isValid()) {
            logFallback(doc, "Thiếu cấu hình BREVO_API_KEY/BREVO_SENDER_EMAIL/BREVO_SENDER_NAME.");
            return;
        }

        String requestBody = buildRequestBody(doc, config);
        if (requestBody == null) {
            logFallback(doc, "Không có email người nhận hợp lệ.");
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
                System.out.println("  [BREVO] Gui email thanh cong. Status=" + status);
            } else {
                System.out.println("  [BREVO] Gui email that bai. Status=" + status + " Body=" + response.body());
            }
        } catch (IOException ex) {
            System.out.println("  [BREVO] Loi gui email: " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("  [BREVO] Loi gui email: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private boolean isEnabled(Document doc) {
        if (doc == null) {
            return false;
        }
        UserPreference preference = doc.userPreference;
        return preference != null && preference.receiveEmail;
    }

    private static void logFallback(Document doc, String reason) {
        System.out.println("  [BREVO] Bo qua gui email: " + reason);
        System.out.println("  [GỬI EMAIL] -> Người nộp (" + safe(doc.applicantEmail)
                + "): Hồ sơ chuyển sang trạng thái " + safe(doc.status));
        System.out.println("  [GỬI EMAIL] -> Cán bộ xử lý (" + safe(doc.officerEmail)
                + "): Hồ sơ chuyển sang trạng thái " + safe(doc.status));
    }

    private static String buildRequestBody(Document doc, BrevoConfig config) {
        JSONArray messageVersions = new JSONArray();
        appendVersion(messageVersions, doc, "Nguoi nop", doc.applicantName, doc.applicantEmail);
        appendVersion(messageVersions, doc, "Can bo xu ly", doc.officerName, doc.officerEmail);

        if (messageVersions.isEmpty()) {
            return null;
        }

        JSONObject sender = new JSONObject().put("name", config.senderName).put("email", config.senderEmail);
        JSONObject body = new JSONObject().put("sender", sender).put("templateId", TEMPLATE_ID).put("messageVersions",
                messageVersions);
        return body.toString();
    }

    private static void appendVersion(JSONArray versions, Document doc, String role, String name, String email) {
        if (isBlank(email)) {
            return;
        }
        JSONObject recipient = new JSONObject().put("email", email).put("name", safe(name));
        JSONArray to = new JSONArray().put(recipient);
        JSONObject params = new JSONObject().put("recipientName", safe(name)).put("role", role)
                .put("documentId", safe(doc.id)).put("documentType", safe(doc.documentType))
                .put("status", safe(doc.status));
        JSONObject version = new JSONObject().put("to", to).put("params", params);
        versions.put(version);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
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
            return new BrevoConfig(System.getenv(ENV_API_KEY), System.getenv(ENV_SENDER_EMAIL),
                    System.getenv(ENV_SENDER_NAME));
        }

        private boolean isValid() {
            return !isBlank(apiKey) && !isBlank(senderEmail) && !isBlank(senderName);
        }
    }
}
