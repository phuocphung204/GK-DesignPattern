package document.extractor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.DisplayName;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.impl.JpgContentExtractor;
import java.io.File;

public class JpgContentExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JpgContentExtractorTest.class);

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ JPG đơn giản")
    void extractTextFromSimpleJpg() {
        JpgContentExtractor extractor = new JpgContentExtractor(ExtractorFactory.ocrService);
        File jpg = new File("server_storage/van-ban.jpg"); // chuẩn bị sample
        assertTrue(jpg.exists(), "tệp JPG mẫu phải tồn tại");
        String text = extractor.extractContent(jpg);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }
}