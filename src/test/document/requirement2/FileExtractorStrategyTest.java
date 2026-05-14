package document.requirement2;

import java.io.File;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import io.github.cdimascio.dotenv.Dotenv;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.core.FileExtractorStrategy;

public class FileExtractorStrategyTest {

    @Test
    @DisplayName("Test sử dụng FileExtractorStrategy để trích xuất nội dung từ file txt")
    public void testFileExtractorStrategy_SupportedFile() {
        File txtFile = new File("support_test/passAll.txt");
        assertTrue(txtFile.exists(), "tệp TXT mẫu phải tồn tại");

        FileExtractorStrategy extractor = ExtractorFactory.getExtractor(getSupportedExtension(txtFile));
        String content = extractor.extractContent(txtFile);

        assertNotNull(content, "nội dung trích xuất không được null");
        assertFalse(content.isBlank(), "nội dung trích xuất không được rỗng");
    }

    @Test
    @DisplayName("Test sử dụng FileExtractorStrategy với file có định dạng pdf (yêu cầu OCR live)")
    public void testFileExtractorStrategy_SupportedPdfFile() {
        Assumptions.assumeTrue(isLiveOcrEnabled() && hasGeminiKey(),
                "Skipping live OCR test (Gemini): set RUN_LIVE_OCR_TESTS=true and provide GEMINI_MODEL_KEY (or -Dgemini.model.key)");
        File pdfFile = new File("support_test/sample-text.pdf");

        assertTrue(pdfFile.exists(), "tệp PDF mẫu phải tồn tại");

        FileExtractorStrategy extractor = ExtractorFactory.getExtractor(getSupportedExtension(pdfFile));
        String content = extractor.extractContent(pdfFile);

        assertNotNull(content, "nội dung trích xuất PDF không được null");
        assertFalse(content.isBlank(), "nội dung trích xuất PDF không được rỗng");
    }

    @Test
    @DisplayName("Test sử dụng FileExtractorStrategy với JPG/PNG (yêu cầu OCR live)")
    public void testFileExtractorStrategy_SupportedImageFiles() {
        Assumptions.assumeTrue(isLiveOcrEnabled() && hasGeminiKey(),
                "Skipping live OCR test (Gemini): set RUN_LIVE_OCR_TESTS=true and provide GEMINI_MODEL_KEY (or -Dgemini.model.key)");

        File jpgFile = new File("support_test/van-ban.jpg");
        assertTrue(jpgFile.exists(), "tệp JPG mẫu phải tồn tại");
        FileExtractorStrategy jpgExtractor = ExtractorFactory.getExtractor(getSupportedExtension(jpgFile));
        String jpgContent = jpgExtractor.extractContent(jpgFile);
        assertNotNull(jpgContent, "nội dung trích xuất JPG không được null");
        assertFalse(jpgContent.isBlank(), "nội dung trích xuất JPG không được rỗng");

        File pngFile = new File("support_test/van-ban.png");
        assertTrue(pngFile.exists(), "tệp PNG mẫu phải tồn tại");
        FileExtractorStrategy pngExtractor = ExtractorFactory.getExtractor(getSupportedExtension(pngFile));
        String pngContent = pngExtractor.extractContent(pngFile);
        assertNotNull(pngContent, "nội dung trích xuất PNG không được null");
        assertFalse(pngContent.isBlank(), "nội dung trích xuất PNG không được rỗng");

    }

    @Test
    @DisplayName("Test sử dụng FileExtractorStrategy với file có định dạng không được hỗ trợ")
    public void testFileExtractorStrategy_UnsupportedFile() {
        File unsupportedFile = new File("support_test/unsupported_file.docx");
        assertTrue(unsupportedFile.exists(), "tệp DOCX mẫu phải tồn tại");

        assertThrows(UnsupportedOperationException.class,
                () -> ExtractorFactory.getExtractor(getSupportedExtension(unsupportedFile)));
    }

    private static String getSupportedExtension(File file) {
        if (file == null) {
            return null;
        }
        String extension = file.getName().lastIndexOf(".") >= 0
                ? file.getName().substring(file.getName().lastIndexOf(".") + 1)
                : null;
        return extension;
    }

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
}
