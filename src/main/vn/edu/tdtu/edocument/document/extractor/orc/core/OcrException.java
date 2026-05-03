package vn.edu.tdtu.edocument.document.extractor.orc.core;

/**
 * Common exception for OCR processing errors.
 */
public class OcrException extends Exception {

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }

    public OcrException(Throwable cause) {
        super(cause);
    }
}
