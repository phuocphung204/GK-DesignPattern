package document.requirement2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import document.requirement2.orc.OrcServiceTextExtractor;

import org.junit.jupiter.api.DisplayName;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.impl.PdfContentExtractor;
import java.io.File;

import io.github.cdimascio.dotenv.Dotenv;

class PdfContentExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(OrcServiceTextExtractor.class);

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
    @DisplayName("Kiểm tra trích xuất văn bản từ PDF đơn giản")
    void extractTextFromSimplePdf() {
        PdfContentExtractor extractor = new PdfContentExtractor(ExtractorFactory.ocrService);
        File pdf = new File("support_test/sample-text.pdf"); // chuẩn bị sample
        assertTrue(pdf.exists(), "tệp PDF mẫu phải tồn tại");
        String text = extractor.extractContent(pdf);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản, từ nhiều trang PDF")
    void extractTextFromMultiPagePdf() {
        PdfContentExtractor extractor = new PdfContentExtractor(ExtractorFactory.ocrService);
        File pdf = new File("support_test/nhieu-trang-co-text-player.pdf"); // chuẩn bị sample
        assertTrue(pdf.exists(), "tệp PDF mẫu phải tồn tại");
        String text = extractor.extractContent(pdf);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản, 1 trang PDF chỉ có hình ảnh (OCR)")
    void extractTextFromMultiPageImageOnlyPdf() {
        Assumptions.assumeTrue(
            isLiveOcrEnabled() && hasGeminiKey(),
            "Skipping live OCR test (Gemini): set RUN_LIVE_OCR_TESTS=true and provide GEMINI_MODEL_KEY (or -Dgemini.model.key)"
        );
        PdfContentExtractor extractor = new PdfContentExtractor(ExtractorFactory.ocrService);
        File pdf = new File("support_test/chi-co-hinh-anh.pdf"); // chuẩn bị sample
        assertTrue(pdf.exists(), "tệp PDF mẫu phải tồn tại");
        String text = extractor.extractContent(pdf);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }
}