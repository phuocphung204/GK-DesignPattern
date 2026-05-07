package vn.edu.tdtu.edocument.document.extractor.orc.core;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Abstraction for OCR providers. Implementations may use Tess4J, Google Vision,
 * Azure, or any other OCR engine.
 */
public interface IOcrService {

    /**
     * Extract text from a BufferedImage.
     * 
     * @param image image to process
     * @return extracted text (may be empty string if none)
     * @throws OcrException on processing errors
     */
    String extractText(BufferedImage image) throws OcrException;

    /**
     * Extract text from an image file.
     * 
     * @param imageFile image file (jpg/png, etc.)
     * @return extracted text
     * @throws OcrException on processing errors
     */
    String extractText(File imageFile) throws OcrException;
}
