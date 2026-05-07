package vn.edu.tdtu.edocument.document.extractor.core;

import vn.edu.tdtu.edocument.document.extractor.impl.PdfContentExtractor;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.impl.GeminiTextExtractor;

public class ExtractorFactory {
    public static final IOcrService ocrService = new GeminiTextExtractor();

    public static FileExtractorStrategy getExtractor(String fileType) {
        if (fileType == null)
            throw new IllegalArgumentException("File type cannot be null");

        switch (fileType.toLowerCase()) {
        case "pdf":
            return new PdfContentExtractor(ocrService);
        // có thể thêm các case khác cho các loại file khác
        default:
            throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }
    }

}
