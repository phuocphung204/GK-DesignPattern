package document.extractor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import document.extractor.orc.OrcServiceTextExtractor;

import org.junit.jupiter.api.DisplayName;

import vn.edu.tdtu.edocument.document.extractor.core.ExtractorFactory;
import vn.edu.tdtu.edocument.document.extractor.impl.PdfContentExtractor;
import java.io.File;

class PdfContentExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(OrcServiceTextExtractor.class);

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản từ PDF đơn giản")
    void extractTextFromSimplePdf() {
        PdfContentExtractor extractor = new PdfContentExtractor(ExtractorFactory.ocrService);
        File pdf = new File("server_storage/sample-text.pdf"); // chuẩn bị sample
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
        File pdf = new File("server_storage/nhieu-trang-co-text-player.pdf"); // chuẩn bị sample
        assertTrue(pdf.exists(), "tệp PDF mẫu phải tồn tại");
        String text = extractor.extractContent(pdf);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }

    @Test
    @DisplayName("Kiểm tra trích xuất văn bản, 1 trang PDF chỉ có hình ảnh (OCR)")
    void extractTextFromMultiPageImageOnlyPdf() {
        PdfContentExtractor extractor = new PdfContentExtractor(ExtractorFactory.ocrService);
        File pdf = new File("server_storage/chi-co-hinh-anh.pdf"); // chuẩn bị sample
        assertTrue(pdf.exists(), "tệp PDF mẫu phải tồn tại");
        String text = extractor.extractContent(pdf);
        assertNotNull(text, "văn bản trích xuất không được null");
        logger.info("Extracted text: " + text);
        assertFalse(text.isEmpty(), "văn bản trích xuất không được rỗng");
    }
}