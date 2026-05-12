package document.requirement2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.DisplayName;

import io.github.cdimascio.dotenv.Dotenv;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.impl.JpgContentExtractor;
import java.io.File;

public class JpgContentExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JpgContentExtractorTest.class);

    private static boolean isLiveOcrEnabled() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String enabled = dotenv.get("RUN_LIVE_OCR_TESTS");
        if (enabled == null || enabled.isBlank()) {
            enabled = System.getProperty("run.live.ocr.tests");
        }
        if (enabled == null || enabled.isBlank()) {
            enabled = System.getenv("RUN_LIVE_OCR_TESTS");
        }
        return "true".equalsIgnoreCase(enabled);
    }

    private static boolean hasGeminiKey() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String key = dotenv.get("GEMINI_MODEL_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("gemini.model.key");
        }
        if (key == null || key.isBlank()) {
            key = System.getenv("GEMINI_MODEL_KEY");
        }
        return key != null && !key.isBlank();
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ JPG đơn giản")
    void extractTextFromSimpleJpg() {
        Assumptions.assumeTrue(
            isLiveOcrEnabled() && hasGeminiKey(),
            "Skipping live OCR test (Gemini): set RUN_LIVE_OCR_TESTS=true and provide GEMINI_MODEL_KEY (or -Dgemini.model.key)"
        );
        JpgContentExtractor extractor = new JpgContentExtractor(ExtractorFactory.ocrService);
        File jpg = new File("support_test/van-ban.jpg"); // chuẩn bị sample
        assertTrue(jpg.exists(), "tệp JPG mẫu phải tồn tại");
        String text = extractor.extractContent(jpg);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }
}