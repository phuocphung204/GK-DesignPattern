package vn.edu.tdtu.edocument.document.extractor.orc.impl;

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
import io.github.cdimascio.dotenv.Dotenv;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;

public class GeminiTextExtractor implements IOcrService {
    private static final String API_KEY = resolveApiKey();
    private static final String MODEL = "gemini-2.5-flash";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL
            + ":generateContent?key=" + API_KEY;

    @Override
    public String extractText(BufferedImage image) throws OcrException {
        if (image == null) {
            throw new OcrException("Image is null");
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new OcrException("Unsupported image format for OCR");
            }
            return extractText(output.toByteArray(), "image/png");
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
            String mime = Files.probeContentType(imageFile.toPath());
            if (mime == null || mime.isBlank()) {
                mime = "image/png";
            }
            return extractText(data, mime);
        } catch (IOException e) {
            throw new OcrException("Failed to read image file for OCR", e);
        }
    }

    private String extractText(byte[] imageBytes, String mimeType) throws OcrException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new OcrException("Missing GEMINI_MODEL_KEY. Set GEMINI_MODEL_KEY in .env or -Dgemini.model.key");
        }

        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject inlineData = new JSONObject().put("mimeType", mimeType).put("data", base64);

        JSONArray parts = new JSONArray()
                .put(new JSONObject().put("text", "Extract all text from this image. Return only plain text."))
                .put(new JSONObject().put("inlineData", inlineData));

        JSONObject payload = new JSONObject().put("contents",
                new JSONArray().put(new JSONObject().put("parts", parts)));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString())).build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OcrException(
                        "Gemini OCR request failed: HTTP " + response.statusCode() + " - " + response.body());
            }

            JSONObject root = new JSONObject(response.body());
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "";
            }

            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            if (content == null) {
                return "";
            }

            JSONArray respParts = content.optJSONArray("parts");
            if (respParts == null || respParts.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < respParts.length(); i++) {
                String text = respParts.getJSONObject(i).optString("text", "");
                if (!text.isBlank()) {
                    if (sb.length() > 0)
                        sb.append('\n');
                    sb.append(text);
                }
            }

            return sb.toString().trim();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OcrException("Failed to call Gemini OCR", e);
        }
    }

    private static String resolveApiKey() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String key = dotenv.get("GEMINI_MODEL_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("gemini.model.key");
        }
        return key;
    }

}
