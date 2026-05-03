package document.extractor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.DisplayName;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.impl.PngContentExtractor;

import java.io.File;

class PngContentExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(PngContentExtractorTest.class);

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ PNG đơn giản")
    void extractTextFromSimplePng() {
        PngContentExtractor extractor = new PngContentExtractor(ExtractorFactory.ocrService);
        File png = new File("server_storage/anh-chua-van-ban.png"); // chuẩn bị sample
        assertTrue(png.exists(), "tệp PNG mẫu phải tồn tại");
        String text = extractor.extractContent(png);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }
}