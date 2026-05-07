package vn.edu.tdtu.edocument.document.extractor.orc.impl;

import io.github.cdimascio.dotenv.Dotenv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;

/**
 * Trích xuất văn bản từ hình ảnh bằng Google Vision API, phiên bản 1, dùng http
 * client.
 */
public class GoogleVisionTextExtractorV1 implements IOcrService {

    private static final String API_KEY = resolveApiKey();
    private static final String TARGET_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;

    @Override
    public String extractText(BufferedImage image) throws OcrException {
        if (image == null) {
            throw new OcrException("Image is null");
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new OcrException("Unsupported image format for OCR");
            }
            return extractText(output.toByteArray());
        } catch (IOException e) {
            throw new OcrException("Failed to encode image for OCR", e);
        }
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        if (imageFile == null) {
            throw new OcrException("Image file is null");
        }
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new OcrException("Image file does not exist: " + imageFile.getAbsolutePath());
        }

        try {
            byte[] data = Files.readAllBytes(imageFile.toPath());
            return extractText(data);
        } catch (IOException e) {
            throw new OcrException("Failed to read image file for OCR", e);
        }
    }

    private String extractText(byte[] imageBytes) throws OcrException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new OcrException(
                    "Missing Google Vision API key. Set GOOGLE_CLOUD_VISION_API_KEY in .env or use -Dgoogle.cloud.vision.api.key");
        }

        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        JSONObject payload = new JSONObject().put("requests",
                new JSONArray().put(new JSONObject().put("image", new JSONObject().put("content", base64))
                        .put("features", new JSONArray().put(new JSONObject().put("type", "TEXT_DETECTION")))));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TARGET_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString())).build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OcrException("OCR request failed: HTTP " + response.statusCode() + " - " + response.body());
            }

            JSONObject root = new JSONObject(response.body());
            JSONArray responses = root.optJSONArray("responses");
            if (responses == null || responses.isEmpty()) {
                return "";
            }

            JSONObject first = responses.getJSONObject(0);
            if (first.has("error")) {
                JSONObject error = first.getJSONObject("error");
                String message = error.optString("message", "Unknown OCR error");
                throw new OcrException("OCR error: " + message);
            }

            JSONArray annotations = first.optJSONArray("textAnnotations");
            if (annotations == null || annotations.isEmpty()) {
                return "";
            }

            return annotations.getJSONObject(0).optString("description", "");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OcrException("Failed to call Google Vision OCR", e);
        }
    }

    private static String resolveApiKey() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String key = dotenv.get("GOOGLE_CLOUD_VISION_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("google.cloud.vision.api.key");
        }
        return key;
    }

}
