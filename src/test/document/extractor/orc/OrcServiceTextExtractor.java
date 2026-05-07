package document.extractor.orc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import vn.edu.tdtu.edocument.document.extractor.orc.impl.GeminiTextExtractor;
import vn.edu.tdtu.edocument.document.extractor.orc.impl.GoogleVisionTextExtractorV1;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.core.OcrException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

import org.junit.jupiter.api.Assumptions;

public class OrcServiceTextExtractor {
    private static final Logger logger = LoggerFactory.getLogger(OrcServiceTextExtractor.class);

    @Test
    @DisplayName("kiểm tra có lấy được API key từ .env không")
    void testApiKeyFromEnv() {
        Dotenv javaDotenv = Dotenv.load();
        String apiKey = javaDotenv.get("GEMINI_MODEL_KEY");
        assertNotNull(apiKey, "API key phải được thiết lập trong .env");
        assertFalse(apiKey.trim().isEmpty(), "API key không được rỗng");
        logger.info("API key loaded from .env: " + apiKey);
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ hình ảnh bằng Google Vision API (API key từ .env)")
    void testExtractTextFromImage() {
        File img = new File("server_storage/anh-text-xin-chao.png"); // chuẩn bị sample

        // Bỏ test nếu file ảnh không tồn tại
        Assumptions.assumeTrue(img.exists() && img.isFile(), "Không tìm thấy ảnh test: " + img.getAbsolutePath());

        IOcrService ocr = new GoogleVisionTextExtractorV1();

        try {
            String text = ocr.extractText(img);
            // In ra để debug khi chạy
            System.out.println("OCR result: \"" + text + "\"");

            // Ít nhất phải trả về chuỗi (có thể chứa newline/spaces) — kiểm tra có ký tự
            // nhìn thấy
            String normalized = text == null ? "" : text.trim();
            assertFalse(normalized.isEmpty(), "Văn bản trích xuất không được rỗng");
        } catch (OcrException e) {
            fail("OcrException thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ hình ảnh bằng Gemini (API key từ .env)")
    void testExtractTextFromImageWithGemini() {
        File img = new File("server_storage/anh-text-xin-chao.png"); // chuẩn bị sample

        // Bỏ test nếu file ảnh không tồn tại
        Assumptions.assumeTrue(img.exists() && img.isFile(), "Không tìm thấy ảnh test: " + img.getAbsolutePath());

        IOcrService ocr = new GeminiTextExtractor();

        try {
            String text = ocr.extractText(img);
            // In ra để debug khi chạy
            // System.out.println("Văn bản trích xuất được: \"" + text + "\"");

            String normalized = text == null ? "" : text.trim();
            logger.info("Văn bản trích xuất được: " + normalized);
            assertFalse(normalized.isEmpty(), "Văn bản trích xuất từ Gemini không được rỗng");
        } catch (OcrException e) {
            fail("OcrException thrown by Gemini: " + e.getMessage());
        }
    }
}