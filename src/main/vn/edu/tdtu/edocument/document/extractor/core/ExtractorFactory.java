package vn.edu.tdtu.edocument.document.extractor.core;

import vn.edu.tdtu.edocument.document.extractor.impl.JpgContentExtractor;
import vn.edu.tdtu.edocument.document.extractor.impl.PdfContentExtractor;
import vn.edu.tdtu.edocument.document.extractor.impl.PngContentExtractor;
import vn.edu.tdtu.edocument.document.extractor.impl.TxtContentExtractor;
import vn.edu.tdtu.edocument.document.extractor.orc.core.IOcrService;
import vn.edu.tdtu.edocument.document.extractor.orc.impl.GeminiTextExtractor;
import vn.edu.tdtu.edocument.model.enums.DocumentExtension;

public class ExtractorFactory {
    public static final IOcrService ocrService = new GeminiTextExtractor();

    public static FileExtractorStrategy getExtractor(DocumentExtension docExtension) {
        if (docExtension == null)
            throw new IllegalArgumentException("File type cannot be null");

        switch (docExtension) {
        case PDF:
            return new PdfContentExtractor(ocrService);
        case PNG:
            return new PngContentExtractor(ocrService);
        case JPG:
            return new JpgContentExtractor(ocrService);
        case TXT:
            return new TxtContentExtractor();
        default:
            throw new UnsupportedOperationException("Unsupported file type: " + docExtension);
        }
    }

}
